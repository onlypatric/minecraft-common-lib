package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.dialog.DialogCallbacks;
import dev.patric.commonlib.api.dialog.DialogCloseEvent;
import dev.patric.commonlib.api.dialog.DialogCloseReason;
import dev.patric.commonlib.api.dialog.DialogEvent;
import dev.patric.commonlib.api.dialog.DialogEventResult;
import dev.patric.commonlib.api.dialog.DialogOpenRequest;
import dev.patric.commonlib.api.dialog.DialogResponse;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSession;
import dev.patric.commonlib.api.dialog.DialogSessionStatus;
import dev.patric.commonlib.api.dialog.DialogSubmission;
import dev.patric.commonlib.api.dialog.DialogSubmitEvent;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import dev.patric.commonlib.api.dialog.DialogTimeoutEvent;
import dev.patric.commonlib.api.dialog.DialogInputSpec;
import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.lifecycle.dialog.DialogPolicyRoutedEvent;
import dev.patric.commonlib.runtime.dialog.DefaultDialogResponse;
import dev.patric.commonlib.runtime.dialog.DialogTemplateCompiler;
import dev.patric.commonlib.runtime.dialog.DialogTemplateValidator;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Default thread-safe service for Paper dialog sessions.
 */
public final class DefaultDialogService implements DialogService {

    private final JavaPlugin plugin;
    private final CommonScheduler scheduler;
    private final EventRouter eventRouter;
    private final RuntimeLogger logger;
    private final DialogTemplateRegistry templateRegistry;
    private final CapabilityRegistry capabilityRegistry;
    private final DialogTemplateCompiler compiler;
    private final DialogTemplateValidator validator;
    private final Map<UUID, SessionRecord> sessions;

    /**
     * Creates a dialog service bound to runtime services.
     *
     * @param plugin owning plugin.
     * @param scheduler scheduler facade.
     * @param eventRouter policy router.
     * @param logger runtime logger.
     * @param templateRegistry template registry.
     * @param capabilityRegistry capability registry.
     */
    public DefaultDialogService(
            JavaPlugin plugin,
            CommonScheduler scheduler,
            EventRouter eventRouter,
            RuntimeLogger logger,
            DialogTemplateRegistry templateRegistry,
            CapabilityRegistry capabilityRegistry
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.eventRouter = Objects.requireNonNull(eventRouter, "eventRouter");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.templateRegistry = Objects.requireNonNull(templateRegistry, "templateRegistry");
        this.capabilityRegistry = Objects.requireNonNull(capabilityRegistry, "capabilityRegistry");
        this.compiler = new DialogTemplateCompiler();
        this.validator = new DialogTemplateValidator();
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public DialogSession open(DialogOpenRequest request) {
        Objects.requireNonNull(request, "request");

        closeAllByPlayer(request.playerId(), DialogCloseReason.REPLACED);

        DialogTemplate template = request.template();
        validator.validate(template, templateRegistry);

        UUID sessionId = UUID.randomUUID();
        long now = System.currentTimeMillis();
        SessionRecord record = new SessionRecord(sessionId, request, now);
        sessions.put(sessionId, record);

        Player player = plugin.getServer().getPlayer(request.playerId());
        if (player == null) {
            logger.warn("dialog open failed: player not online for session " + sessionId);
            closeInternal(sessionId, DialogCloseReason.ERROR);
            return record.snapshotWithStatus(DialogSessionStatus.ERROR);
        }

        io.papermc.paper.dialog.Dialog dialog;
        try {
            dialog = compiler.compile(
                    template,
                    key -> key.equals(template.templateKey()) ? Optional.of(template) : templateRegistry.find(key),
                    (actionId, inputSpecs, additions) -> createCustomAction(sessionId, actionId, inputSpecs, additions)
            );
        } catch (RuntimeException ex) {
            logger.error("dialog open failed for template " + template.templateKey(), ex);
            markDialogUnavailable(ex);
            closeInternal(sessionId, DialogCloseReason.ERROR);
            return record.snapshotWithStatus(DialogSessionStatus.ERROR);
        }

        try {
            player.showDialog(dialog);
        } catch (RuntimeException ex) {
            logger.warn("dialog backend rejected show operation for template " + template.templateKey());
            markDialogUnavailable(ex);
        }

        if (request.timeoutTicks() > 0L) {
            TaskHandle timeoutHandle = scheduler.runSyncLater(request.timeoutTicks(), () -> {
                Optional<DialogSession> snapshot = find(sessionId);
                if (snapshot.isEmpty()) {
                    return;
                }
                publish(new DialogTimeoutEvent(sessionId, record.revision()));
            });
            record.setTimeoutHandle(timeoutHandle);
        }

        invokeOnOpen(record);
        return record.snapshot();
    }

    @Override
    public Optional<DialogSession> find(UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        SessionRecord record = sessions.get(sessionId);
        return record == null ? Optional.empty() : Optional.of(record.snapshot());
    }

    @Override
    public List<DialogSession> activeByPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        List<DialogSession> active = new ArrayList<>();
        for (SessionRecord record : sessions.values()) {
            if (record.belongsTo(playerId) && record.isOpen()) {
                active.add(record.snapshot());
            }
        }
        return List.copyOf(active);
    }

    @Override
    public DialogEventResult publish(DialogEvent event) {
        Objects.requireNonNull(event, "event");

        SessionRecord record = sessions.get(event.sessionId());
        if (record == null) {
            return DialogEventResult.SESSION_NOT_FOUND;
        }

        DialogPolicyRoutedEvent routedEvent = new DialogPolicyRoutedEvent(event);
        PolicyDecision decision = eventRouter.route(routedEvent);
        if (decision.denied()) {
            return DialogEventResult.DENIED_BY_POLICY;
        }

        DialogEventResult precheck = record.precheck(event.expectedRevision());
        if (precheck != DialogEventResult.APPLIED) {
            return precheck;
        }

        if (event instanceof DialogSubmitEvent submitEvent) {
            DialogSubmission submission = submitEvent.submission();
            if (!record.belongsTo(submission.playerId())) {
                return DialogEventResult.INVALID_PAYLOAD;
            }
            record.bumpRevision();
            record.touch();
            try {
                record.callbacks().onSubmit(record.snapshot(), submission);
            } catch (RuntimeException ex) {
                logger.error("dialog submit callback failed for session " + event.sessionId(), ex);
                invokeOnError(record, ex);
                closeInternal(event.sessionId(), DialogCloseReason.ERROR);
                return DialogEventResult.HANDLER_ERROR;
            }
            closeInternal(event.sessionId(), DialogCloseReason.SUBMITTED);
            return DialogEventResult.APPLIED;
        }

        if (event instanceof DialogTimeoutEvent) {
            return closeInternal(event.sessionId(), DialogCloseReason.TIMEOUT)
                    ? DialogEventResult.APPLIED
                    : DialogEventResult.SESSION_NOT_OPEN;
        }

        if (event instanceof DialogCloseEvent closeEvent) {
            return closeInternal(event.sessionId(), closeEvent.reason())
                    ? DialogEventResult.APPLIED
                    : DialogEventResult.SESSION_NOT_OPEN;
        }

        return DialogEventResult.INVALID_PAYLOAD;
    }

    @Override
    public boolean close(UUID sessionId, DialogCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        return closeInternal(sessionId, reason);
    }

    @Override
    public int closeAllByPlayer(UUID playerId, DialogCloseReason reason) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (SessionRecord record : List.copyOf(sessions.values())) {
            if (record.belongsTo(playerId) && closeInternal(record.sessionId(), reason)) {
                closed++;
            }
        }
        return closed;
    }

    @Override
    public int closeAll(DialogCloseReason reason) {
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (UUID sessionId : List.copyOf(sessions.keySet())) {
            if (closeInternal(sessionId, reason)) {
                closed++;
            }
        }
        return closed;
    }

    void onPlayerQuit(UUID playerId) {
        closeAllByPlayer(Objects.requireNonNull(playerId, "playerId"), DialogCloseReason.QUIT);
    }

    private DialogAction createCustomAction(
            UUID sessionId,
            String actionId,
            List<DialogInputSpec> inputSpecs,
            Map<String, String> additions
    ) {
        Objects.requireNonNull(additions, "additions");
        ClickCallback.Options options = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(Duration.ofMinutes(10))
                .build();

        return DialogAction.customClick((response, audience) -> {
            if (!(audience instanceof Player player)) {
                return;
            }
            handleCustomSubmit(sessionId, actionId, inputSpecs, response, player);
        }, options);
    }

    private void handleCustomSubmit(
            UUID sessionId,
            String actionId,
            List<DialogInputSpec> inputSpecs,
            DialogResponseView response,
            Player player
    ) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null) {
            return;
        }

        DialogResponse typedResponse = new DefaultDialogResponse(response, inputSpecs);
        DialogSubmission submission = new DialogSubmission(
                sessionId,
                player.getUniqueId(),
                actionId,
                typedResponse,
                System.currentTimeMillis()
        );
        publish(new DialogSubmitEvent(sessionId, record.revision(), submission));
    }

    private boolean closeInternal(UUID sessionId, DialogCloseReason reason) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null) {
            return false;
        }
        if (!record.beginClosing()) {
            return false;
        }

        record.cancelTimeout();
        safeCloseDialog(record.playerId());
        record.finishClosing(reason);
        sessions.remove(sessionId, record);

        invokeOnClose(record, reason);
        return true;
    }

    private void safeCloseDialog(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null) {
            return;
        }
        try {
            player.closeDialog();
        } catch (RuntimeException ex) {
            logger.error("dialog close backend call failed for player " + playerId, ex);
            markDialogUnavailable(ex);
        }
    }

    private void invokeOnOpen(SessionRecord record) {
        try {
            record.callbacks().onOpen(record.snapshot());
        } catch (RuntimeException ex) {
            logger.error("dialog onOpen callback failed for session " + record.sessionId(), ex);
            invokeOnError(record, ex);
        }
    }

    private void invokeOnClose(SessionRecord record, DialogCloseReason reason) {
        try {
            record.callbacks().onClose(record.snapshotWithStatus(record.status()), reason);
        } catch (RuntimeException ex) {
            logger.error("dialog onClose callback failed for session " + record.sessionId(), ex);
            invokeOnError(record, ex);
        }
    }

    private void invokeOnError(SessionRecord record, Throwable error) {
        try {
            record.callbacks().onError(record.snapshotWithStatus(DialogSessionStatus.ERROR), error);
        } catch (RuntimeException callbackError) {
            logger.error("dialog onError callback failed for session " + record.sessionId(), callbackError);
        }
    }

    private void markDialogUnavailable(Throwable throwable) {
        String reason = "binding-failed:paper-dialog:" + throwable.getClass().getSimpleName();
        capabilityRegistry.publish(StandardCapabilities.DIALOG, CapabilityStatus.unavailable(reason));
    }

    private static final class SessionRecord {

        private final UUID sessionId;
        private final UUID playerId;
        private final String templateKey;
        private final DialogCallbacks callbacks;
        private final long openedAtEpochMilli;
        private final long timeoutTicks;

        private DialogSessionStatus status;
        private long lastInteractionEpochMilli;
        private long revision;
        private TaskHandle timeoutHandle;

        private SessionRecord(UUID sessionId, DialogOpenRequest request, long now) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.playerId = request.playerId();
            this.templateKey = request.template().templateKey();
            this.callbacks = request.callbacks();
            this.openedAtEpochMilli = now;
            this.lastInteractionEpochMilli = now;
            this.timeoutTicks = request.timeoutTicks();
            this.status = DialogSessionStatus.OPEN;
            this.revision = 0L;
        }

        private synchronized UUID sessionId() {
            return sessionId;
        }

        private synchronized UUID playerId() {
            return playerId;
        }

        private synchronized DialogCallbacks callbacks() {
            return callbacks;
        }

        private synchronized boolean belongsTo(UUID playerId) {
            return this.playerId.equals(playerId);
        }

        private synchronized boolean isOpen() {
            return status == DialogSessionStatus.OPEN;
        }

        private synchronized long revision() {
            return revision;
        }

        private synchronized DialogSessionStatus status() {
            return status;
        }

        private synchronized void setTimeoutHandle(TaskHandle timeoutHandle) {
            this.timeoutHandle = timeoutHandle;
        }

        private synchronized void cancelTimeout() {
            if (timeoutHandle != null) {
                timeoutHandle.cancel();
            }
        }

        private synchronized void touch() {
            this.lastInteractionEpochMilli = System.currentTimeMillis();
        }

        private synchronized void bumpRevision() {
            this.revision++;
            this.lastInteractionEpochMilli = System.currentTimeMillis();
        }

        private synchronized DialogEventResult precheck(long expectedRevision) {
            if (status != DialogSessionStatus.OPEN) {
                return DialogEventResult.SESSION_NOT_OPEN;
            }
            if (this.revision != expectedRevision) {
                return DialogEventResult.INVALID_PAYLOAD;
            }
            return DialogEventResult.APPLIED;
        }

        private synchronized boolean beginClosing() {
            if (status != DialogSessionStatus.OPEN) {
                return false;
            }
            status = DialogSessionStatus.CLOSING;
            return true;
        }

        private synchronized void finishClosing(DialogCloseReason reason) {
            if (reason == DialogCloseReason.TIMEOUT) {
                status = DialogSessionStatus.TIMED_OUT;
            } else if (reason == DialogCloseReason.ERROR) {
                status = DialogSessionStatus.ERROR;
            } else {
                status = DialogSessionStatus.CLOSED;
            }
            lastInteractionEpochMilli = System.currentTimeMillis();
            revision++;
        }

        private synchronized DialogSession snapshot() {
            return new DialogSession(
                    sessionId,
                    playerId,
                    templateKey,
                    status,
                    openedAtEpochMilli,
                    lastInteractionEpochMilli,
                    timeoutTicks
            );
        }

        private synchronized DialogSession snapshotWithStatus(DialogSessionStatus overrideStatus) {
            return new DialogSession(
                    sessionId,
                    playerId,
                    templateKey,
                    overrideStatus,
                    openedAtEpochMilli,
                    lastInteractionEpochMilli,
                    timeoutTicks
            );
        }
    }
}
