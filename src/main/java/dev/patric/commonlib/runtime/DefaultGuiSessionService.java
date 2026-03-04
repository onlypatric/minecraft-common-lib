package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.dialog.DialogCallbacks;
import dev.patric.commonlib.api.dialog.DialogOpenRequest;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSubmission;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import dev.patric.commonlib.api.gui.BackMenuAction;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.CloseEventPortable;
import dev.patric.commonlib.api.gui.CloseGuiAction;
import dev.patric.commonlib.api.gui.CustomAction;
import dev.patric.commonlib.api.gui.DialogOpenOptionsMapping;
import dev.patric.commonlib.api.gui.DialogResponseBinding;
import dev.patric.commonlib.api.gui.DisconnectEventPortable;
import dev.patric.commonlib.api.gui.DoubleClickEventPortable;
import dev.patric.commonlib.api.gui.DropEventPortable;
import dev.patric.commonlib.api.gui.GuiAction;
import dev.patric.commonlib.api.gui.GuiBehaviorPolicy;
import dev.patric.commonlib.api.gui.GuiClickEvent;
import dev.patric.commonlib.api.gui.GuiCloseEvent;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDefinitionRegistry;
import dev.patric.commonlib.api.gui.GuiDisconnectEvent;
import dev.patric.commonlib.api.gui.GuiEvent;
import dev.patric.commonlib.api.gui.GuiEventResult;
import dev.patric.commonlib.api.gui.GuiInteractionEvent;
import dev.patric.commonlib.api.gui.GuiInteractionResult;
import dev.patric.commonlib.api.gui.GuiItemView;
import dev.patric.commonlib.api.gui.GuiLayout;
import dev.patric.commonlib.api.gui.GuiOpenOptions;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.GuiSessionStatus;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.GuiTitle;
import dev.patric.commonlib.api.gui.GuiTimeoutEvent;
import dev.patric.commonlib.api.gui.GuiUpdateResult;
import dev.patric.commonlib.api.gui.HotbarSwapEventPortable;
import dev.patric.commonlib.api.gui.InventoryDragEventPortable;
import dev.patric.commonlib.api.gui.OpenDialogAction;
import dev.patric.commonlib.api.gui.OpenSubMenuAction;
import dev.patric.commonlib.api.gui.RunCommandAction;
import dev.patric.commonlib.api.gui.SlotClickEvent;
import dev.patric.commonlib.api.gui.SlotDefinition;
import dev.patric.commonlib.api.gui.SlotInteractionPolicy;
import dev.patric.commonlib.api.gui.SlotTransferType;
import dev.patric.commonlib.api.gui.TimeoutEventPortable;
import dev.patric.commonlib.api.gui.ToggleStateAction;
import dev.patric.commonlib.api.gui.UpdateStateAction;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.lifecycle.gui.GuiInteractionPolicyRoutedEvent;
import dev.patric.commonlib.lifecycle.gui.GuiPolicyRoutedEvent;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Default thread-safe GUI session service.
 */
public final class DefaultGuiSessionService implements GuiSessionService {

    private final Map<UUID, SessionRecord> sessions;
    private final CommonScheduler scheduler;
    private final EventRouter eventRouter;
    private final RuntimeLogger logger;
    private final GuiPort guiPort;
    private final ServiceRegistry services;

    /**
     * Creates a GUI session service bound to runtime services.
     *
     * @param scheduler scheduler facade.
     * @param eventRouter policy router.
     * @param logger runtime logger.
     * @param guiPort backend gui port.
     * @param services runtime service registry.
     */
    public DefaultGuiSessionService(
            CommonScheduler scheduler,
            EventRouter eventRouter,
            RuntimeLogger logger,
            GuiPort guiPort,
            ServiceRegistry services
    ) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.eventRouter = Objects.requireNonNull(eventRouter, "eventRouter");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.guiPort = Objects.requireNonNull(guiPort, "guiPort");
        this.services = Objects.requireNonNull(services, "services");
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public GuiSession open(GuiDefinition definition, UUID playerId, GuiOpenOptions options) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(playerId, "playerId");
        GuiOpenOptions normalizedOptions = options == null ? GuiOpenOptions.defaults() : options;

        if (normalizedOptions.replaceExisting()) {
            closeAllByPlayer(playerId, GuiCloseReason.REPLACED);
        }

        UUID sessionId = UUID.randomUUID();
        long now = System.currentTimeMillis();
        SessionRecord record = new SessionRecord(sessionId, playerId, definition, normalizedOptions, now);
        sessions.put(sessionId, record);

        if (normalizedOptions.timeoutTicks() > 0L) {
            TaskHandle timeoutHandle = scheduler.runSyncLater(normalizedOptions.timeoutTicks(), () -> {
                Optional<GuiSession> snapshot = find(sessionId);
                if (snapshot.isEmpty()) {
                    return;
                }
                interact(new TimeoutEventPortable(sessionId, snapshot.get().state().revision()));
            });
            record.setTimeoutHandle(timeoutHandle);
        }

        boolean opened = safeOpen(record);
        if (!opened) {
            logger.warn("gui open rejected by backend: " + definition.key() + " for " + playerId);
            closeInternal(sessionId, GuiCloseReason.ERROR);
            return record.snapshotClosed();
        }

        return record.snapshot();
    }

    @Override
    public GuiSession open(GuiOpenRequest request) {
        Objects.requireNonNull(request, "request");

        GuiDefinition definition = new GuiDefinition(
                request.viewKey(),
                GuiLayout.chestRows(6),
                new GuiTitle(request.viewKey()),
                Map.of(),
                new GuiBehaviorPolicy(false, true, true)
        );
        GuiOpenOptions options = new GuiOpenOptions(
                request.timeoutTicks(),
                true,
                true,
                Locale.ENGLISH,
                Map.of()
        );

        GuiSession session = open(definition, request.playerId(), options);
        if (!request.initialState().equals(GuiState.empty())) {
            update(session.sessionId(), request.initialState(), session.state().revision());
        }
        return find(session.sessionId()).orElse(session);
    }

    @Override
    public Optional<GuiSession> find(UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        SessionRecord record = sessions.get(sessionId);
        return record == null ? Optional.empty() : Optional.of(record.snapshot());
    }

    @Override
    public List<GuiSession> activeByPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        List<GuiSession> active = new ArrayList<>();
        for (SessionRecord record : sessions.values()) {
            if (record.belongsTo(playerId) && record.isOpen()) {
                active.add(record.snapshot());
            }
        }
        return List.copyOf(active);
    }

    @Override
    public GuiUpdateResult update(UUID sessionId, GuiState nextState, long expectedRevision) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(nextState, "nextState");

        SessionRecord record = sessions.get(sessionId);
        if (record == null) {
            return GuiUpdateResult.SESSION_NOT_FOUND;
        }

        GuiUpdateResult result = record.update(nextState, expectedRevision);
        if (result != GuiUpdateResult.APPLIED) {
            return result;
        }

        boolean rendered = safeRender(sessionId, record.snapshot().state());
        if (!rendered) {
            logger.warn("gui render rejected by backend for session " + sessionId);
        }
        return GuiUpdateResult.APPLIED;
    }

    @Override
    public GuiInteractionResult interact(GuiInteractionEvent event) {
        Objects.requireNonNull(event, "event");

        SessionRecord record = sessions.get(event.sessionId());
        if (record == null) {
            return GuiInteractionResult.SESSION_NOT_FOUND;
        }

        GuiInteractionPolicyRoutedEvent routedEvent = new GuiInteractionPolicyRoutedEvent(event);
        PolicyDecision v2Decision = eventRouter.route(routedEvent);
        if (v2Decision.denied()) {
            return GuiInteractionResult.DENIED_BY_POLICY;
        }

        GuiEvent legacyEvent = toLegacyEvent(event);
        if (legacyEvent != null) {
            PolicyDecision legacyDecision = eventRouter.route(new GuiPolicyRoutedEvent(legacyEvent));
            if (legacyDecision.denied()) {
                return GuiInteractionResult.DENIED_BY_POLICY;
            }
        }

        if (event instanceof DisconnectEventPortable disconnect && !record.belongsTo(disconnect.playerId())) {
            return GuiInteractionResult.SESSION_NOT_FOUND;
        }

        GuiInteractionResult precheck = record.precheck(event.expectedRevision());
        if (precheck != GuiInteractionResult.APPLIED) {
            return precheck;
        }

        if (event instanceof CloseEventPortable closeEvent) {
            return closeInternal(closeEvent.sessionId(), closeEvent.reason())
                    ? GuiInteractionResult.APPLIED
                    : GuiInteractionResult.SESSION_NOT_OPEN;
        }
        if (event instanceof TimeoutEventPortable timeoutEvent) {
            return closeInternal(timeoutEvent.sessionId(), GuiCloseReason.TIMEOUT)
                    ? GuiInteractionResult.APPLIED
                    : GuiInteractionResult.SESSION_NOT_OPEN;
        }
        if (event instanceof DisconnectEventPortable disconnectEvent) {
            return closeInternal(disconnectEvent.sessionId(), GuiCloseReason.DISCONNECT)
                    ? GuiInteractionResult.APPLIED
                    : GuiInteractionResult.SESSION_NOT_OPEN;
        }

        if (event instanceof SlotClickEvent slotClick) {
            return applySlotClick(record, slotClick);
        }
        if (event instanceof InventoryDragEventPortable dragEvent) {
            return applyDrag(record, dragEvent);
        }
        if (event instanceof HotbarSwapEventPortable hotbarSwapEvent) {
            return evaluateTransfer(record, hotbarSwapEvent.slot(), SlotTransferType.DEPOSIT)
                    ? touch(record)
                    : GuiInteractionResult.DENIED_BY_POLICY;
        }
        if (event instanceof DropEventPortable dropEvent) {
            return evaluateTransfer(record, dropEvent.slot(), SlotTransferType.TAKE)
                    ? touch(record)
                    : GuiInteractionResult.DENIED_BY_POLICY;
        }
        if (event instanceof DoubleClickEventPortable) {
            GuiBehaviorPolicy policy = record.definition().behaviorPolicy();
            if (!policy.allowDoubleClick()) {
                return GuiInteractionResult.DENIED_BY_POLICY;
            }
            return touch(record);
        }

        return GuiInteractionResult.INVALID_ACTION;
    }

    @Override
    public GuiEventResult publish(GuiEvent event) {
        Objects.requireNonNull(event, "event");

        GuiInteractionEvent interactionEvent = switch (event) {
            case GuiClickEvent clickEvent -> new SlotClickEvent(
                    clickEvent.sessionId(),
                    clickEvent.expectedRevision(),
                    clickEvent.slot(),
                    clickEvent.action(),
                    SlotTransferType.NONE
            );
            case GuiCloseEvent closeEvent -> new CloseEventPortable(
                    closeEvent.sessionId(),
                    closeEvent.expectedRevision(),
                    closeEvent.reason()
            );
            case GuiTimeoutEvent timeoutEvent -> new TimeoutEventPortable(
                    timeoutEvent.sessionId(),
                    timeoutEvent.expectedRevision()
            );
            case GuiDisconnectEvent disconnectEvent -> new DisconnectEventPortable(
                    disconnectEvent.sessionId(),
                    disconnectEvent.expectedRevision(),
                    disconnectEvent.playerId()
            );
        };

        return toLegacyResult(interact(interactionEvent));
    }

    @Override
    public boolean close(UUID sessionId, GuiCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        return closeInternal(sessionId, reason);
    }

    @Override
    public int closeAllByPlayer(UUID playerId, GuiCloseReason reason) {
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
    public int closeAll(GuiCloseReason reason) {
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (UUID sessionId : List.copyOf(sessions.keySet())) {
            if (closeInternal(sessionId, reason)) {
                closed++;
            }
        }
        return closed;
    }

    private GuiInteractionResult applySlotClick(SessionRecord record, SlotClickEvent clickEvent) {
        if (!evaluateTransfer(record, clickEvent.slot(), clickEvent.transferType())) {
            return GuiInteractionResult.DENIED_BY_POLICY;
        }

        SlotDefinition slotDefinition = record.definition().slots().get(clickEvent.slot());
        if (slotDefinition != null) {
            for (GuiAction action : slotDefinition.actions()) {
                GuiInteractionResult result = executeAction(record, action, clickEvent.slot());
                if (result != GuiInteractionResult.APPLIED) {
                    return result;
                }
            }
        }

        return touch(record);
    }

    private GuiInteractionResult applyDrag(SessionRecord record, InventoryDragEventPortable dragEvent) {
        if (dragEvent.slots().isEmpty()) {
            return touch(record);
        }
        for (int slot : dragEvent.slots()) {
            if (!evaluateTransfer(record, slot, SlotTransferType.DEPOSIT)) {
                return GuiInteractionResult.DENIED_BY_POLICY;
            }
        }
        return touch(record);
    }

    private GuiInteractionResult touch(SessionRecord record) {
        record.touch();
        return GuiInteractionResult.APPLIED;
    }

    private boolean evaluateTransfer(SessionRecord record, int slot, SlotTransferType transferType) {
        SlotDefinition slotDefinition = record.definition().slots().get(slot);
        if (slotDefinition == null) {
            return !record.definition().behaviorPolicy().lockUnknownSlots();
        }

        return switch (slotDefinition.interaction()) {
            case LOCKED -> false;
            case BUTTON_ONLY, INPUT_DIALOG -> transferType == SlotTransferType.NONE;
            case TAKE_ONLY -> transferType == SlotTransferType.NONE
                    || transferType == SlotTransferType.TAKE
                    || transferType == SlotTransferType.TAKE_DEPOSIT;
            case DEPOSIT_ONLY -> transferType == SlotTransferType.DEPOSIT
                    || transferType == SlotTransferType.TAKE_DEPOSIT;
            case TAKE_DEPOSIT -> true;
        };
    }

    private GuiInteractionResult executeAction(SessionRecord record, GuiAction action, int slot) {
        try {
            if (action instanceof RunCommandAction commandAction) {
                return executeCommandAction(record, commandAction) ? GuiInteractionResult.APPLIED : GuiInteractionResult.INVALID_ACTION;
            }
            if (action instanceof OpenDialogAction dialogAction) {
                return executeDialogAction(record, dialogAction) ? GuiInteractionResult.APPLIED : GuiInteractionResult.INVALID_ACTION;
            }
            if (action instanceof ToggleStateAction toggleStateAction) {
                return executeToggleAction(record, toggleStateAction, slot);
            }
            if (action instanceof OpenSubMenuAction openSubMenuAction) {
                return executeOpenSubMenuAction(record, openSubMenuAction);
            }
            if (action instanceof BackMenuAction) {
                return executeBackMenuAction(record);
            }
            if (action instanceof UpdateStateAction stateAction) {
                applyStateAction(record, stateAction);
                return GuiInteractionResult.APPLIED;
            }
            if (action instanceof CloseGuiAction closeAction) {
                closeInternal(record.sessionId(), closeAction.reason());
                return GuiInteractionResult.APPLIED;
            }
            if (action instanceof CustomAction customAction) {
                logger.debug("gui custom action invoked: " + customAction.actionKey());
                return GuiInteractionResult.APPLIED;
            }
        } catch (RuntimeException ex) {
            logger.error("gui action failed for session " + record.sessionId(), ex);
            return GuiInteractionResult.INVALID_ACTION;
        }
        return GuiInteractionResult.INVALID_ACTION;
    }

    private boolean executeCommandAction(SessionRecord record, RunCommandAction commandAction) {
        String command = applyPlaceholders(record, commandAction.commandTemplate());
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.isBlank()) {
            return false;
        }

        if (commandAction.asConsole()) {
            CommandSender sender = Bukkit.getConsoleSender();
            return Bukkit.dispatchCommand(sender, command);
        }

        Player player = Bukkit.getPlayer(record.playerId());
        if (player == null || !player.isOnline()) {
            return false;
        }
        return player.performCommand(command);
    }

    private boolean executeDialogAction(SessionRecord record, OpenDialogAction dialogAction) {
        Optional<DialogService> dialogService = services.find(DialogService.class);
        Optional<DialogTemplateRegistry> registry = services.find(DialogTemplateRegistry.class);
        if (dialogService.isEmpty() || registry.isEmpty()) {
            return false;
        }

        Optional<DialogTemplate> template = registry.get().find(dialogAction.dialogTemplateKey());
        if (template.isEmpty()) {
            return false;
        }

        DialogOpenOptionsMapping mapping = dialogAction.mapping();
        if (!validateDialogBindings(template.get(), mapping.responseBindings())) {
            return false;
        }
        Map<String, String> placeholders = new HashMap<>(mapping.staticPlaceholders());
        if (mapping.includeGuiPlaceholders()) {
            placeholders.putAll(record.placeholders());
        }
        if (mapping.includeGuiState()) {
            placeholders.putAll(record.state().data());
        }

        UUID guiSessionId = record.sessionId();
        dialogService.get().open(new DialogOpenRequest(
                record.playerId(),
                template.get(),
                0L,
                record.locale(),
                placeholders,
                new DialogCallbacks() {
                    @Override
                    public void onSubmit(dev.patric.commonlib.api.dialog.DialogSession session, DialogSubmission submission) {
                        applyDialogBindings(guiSessionId, mapping.responseBindings(), submission);
                    }
                }
        ));
        return true;
    }

    private GuiInteractionResult executeToggleAction(SessionRecord record, ToggleStateAction action, int slot) {
        SlotDefinition currentSlot = record.definition().slots().get(slot);
        boolean currentlyOn = isToggleCurrentlyOn(record, action, currentSlot);
        String nextValue = currentlyOn ? action.offValue() : action.onValue();
        GuiItemView nextItem = currentlyOn ? action.offItem() : action.onItem();

        record.applyToggle(slot, action.stateKey(), nextValue, nextItem);

        if (action.rerender()) {
            return safeOpen(record) ? GuiInteractionResult.APPLIED : GuiInteractionResult.INVALID_ACTION;
        }
        return safeRender(record.sessionId(), record.state()) ? GuiInteractionResult.APPLIED : GuiInteractionResult.INVALID_ACTION;
    }

    private GuiInteractionResult executeOpenSubMenuAction(SessionRecord record, OpenSubMenuAction action) {
        Optional<GuiDefinitionRegistry> registry = services.find(GuiDefinitionRegistry.class);
        if (registry.isEmpty()) {
            return GuiInteractionResult.INVALID_ACTION;
        }

        Optional<GuiDefinition> target = registry.get().find(action.targetMenuKey());
        if (target.isEmpty()) {
            logger.warn("submenu target not found: " + action.targetMenuKey());
            return GuiInteractionResult.INVALID_ACTION;
        }

        SessionRecord.NavigationFrame previous = record.captureFrame();
        if (record.navigationSize() >= 16) {
            logger.debug("gui navigation stack overflow, dropping oldest frame for session " + record.sessionId());
        }
        record.pushNavigation(previous, 16);

        GuiState nextState = action.inheritState()
                ? GuiState.withData(record.state().revision() + 1L, record.state().data())
                : GuiState.withData(record.state().revision() + 1L, Map.of());
        Map<String, String> nextPlaceholders = action.inheritPlaceholders()
                ? record.placeholders()
                : Map.of();

        record.applyFrame(new SessionRecord.NavigationFrame(
                target.get(),
                nextState,
                nextPlaceholders,
                record.locale()
        ));
        if (safeOpen(record)) {
            return GuiInteractionResult.APPLIED;
        }

        record.popNavigation();
        record.applyFrame(previous);
        return GuiInteractionResult.INVALID_ACTION;
    }

    private GuiInteractionResult executeBackMenuAction(SessionRecord record) {
        Optional<SessionRecord.NavigationFrame> previous = record.popNavigation();
        if (previous.isEmpty()) {
            return GuiInteractionResult.INVALID_ACTION;
        }

        SessionRecord.NavigationFrame current = record.captureFrame();
        SessionRecord.NavigationFrame frame = previous.get();
        GuiState nextState = GuiState.withData(record.state().revision() + 1L, frame.state().data());
        record.applyFrame(new SessionRecord.NavigationFrame(frame.definition(), nextState, frame.placeholders(), frame.locale()));

        if (safeOpen(record)) {
            return GuiInteractionResult.APPLIED;
        }

        record.pushNavigation(frame, 16);
        record.applyFrame(current);
        return GuiInteractionResult.INVALID_ACTION;
    }

    private boolean isToggleCurrentlyOn(SessionRecord record, ToggleStateAction action, SlotDefinition currentSlot) {
        String currentValue = record.state().data().get(action.stateKey());
        if (currentValue != null) {
            return currentValue.equals(action.onValue());
        }
        return currentSlot != null && Objects.equals(currentSlot.item(), action.onItem());
    }

    private boolean validateDialogBindings(DialogTemplate template, List<DialogResponseBinding> bindings) {
        if (bindings.isEmpty()) {
            return true;
        }
        java.util.Set<String> validKeys = template.base().inputs().stream()
                .map(dev.patric.commonlib.api.dialog.DialogInputSpec::key)
                .collect(Collectors.toSet());
        for (DialogResponseBinding binding : bindings) {
            if (!validKeys.contains(binding.responseKey())) {
                logger.warn("dialog binding key not present in template " + template.templateKey() + ": " + binding.responseKey());
                return false;
            }
        }
        return true;
    }

    private void applyDialogBindings(UUID guiSessionId, List<DialogResponseBinding> bindings, DialogSubmission submission) {
        if (bindings.isEmpty()) {
            return;
        }
        SessionRecord current = sessions.get(guiSessionId);
        if (current == null || !current.isOpen()) {
            return;
        }

        Map<String, String> nextData = new HashMap<>(current.state().data());
        for (DialogResponseBinding binding : bindings) {
            Optional<String> mapped = extractResponseValue(submission, binding.responseKey());
            if (mapped.isEmpty()) {
                if (binding.required()) {
                    logger.warn("dialog required response missing: " + binding.responseKey());
                    return;
                }
                continue;
            }
            nextData.put(binding.stateKey(), mapped.get());
        }

        current.replaceState(nextData);
        safeRender(guiSessionId, current.state());
    }

    private Optional<String> extractResponseValue(DialogSubmission submission, String key) {
        Optional<String> asText = submission.response().text(key);
        if (asText.isPresent()) {
            return asText;
        }
        Optional<Boolean> asBool = submission.response().bool(key);
        if (asBool.isPresent()) {
            return Optional.of(Boolean.toString(asBool.get()));
        }
        Optional<Float> asNumber = submission.response().number(key);
        if (asNumber.isPresent()) {
            return Optional.of(Float.toString(asNumber.get()));
        }
        Object fromMap = submission.response().asMap().get(key);
        if (fromMap == null) {
            return Optional.empty();
        }
        return Optional.of(String.valueOf(fromMap));
    }

    private void applyStateAction(SessionRecord record, UpdateStateAction stateAction) {
        Map<String, String> nextData;
        if (stateAction.replace()) {
            nextData = stateAction.data();
        } else {
            nextData = new HashMap<>(record.state().data());
            nextData.putAll(stateAction.data());
        }
        record.replaceState(nextData);
        safeRender(record.sessionId(), record.state());
    }

    private String applyPlaceholders(SessionRecord record, String template) {
        String rendered = template;

        Map<String, String> values = new HashMap<>(record.placeholders());
        values.putAll(record.state().data());
        values.put("playerId", record.playerId().toString());
        values.put("sessionId", record.sessionId().toString());
        values.put("guiKey", record.definition().key());

        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered = rendered.replace("%" + entry.getKey() + "%", entry.getValue());
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return rendered;
    }

    private boolean closeInternal(UUID sessionId, GuiCloseReason reason) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null) {
            return false;
        }
        if (!record.beginClosing()) {
            return false;
        }

        record.cancelTimeout();

        boolean closed = safeClose(sessionId, reason);
        if (!closed) {
            logger.warn("gui close rejected by backend for session " + sessionId + " with reason " + reason);
        }

        record.finishClosing(reason);
        sessions.remove(sessionId, record);
        return true;
    }

    private boolean safeOpen(SessionRecord record) {
        try {
            return guiPort.open(new GuiRenderModel(
                    record.sessionId(),
                    record.playerId(),
                    record.definition(),
                    record.state()
            ));
        } catch (RuntimeException ex) {
            logger.error("gui open threw exception", ex);
            return false;
        }
    }

    private boolean safeRender(UUID sessionId, GuiState state) {
        try {
            return guiPort.render(sessionId, new GuiRenderPatch(sessionId, state));
        } catch (RuntimeException ex) {
            logger.error("gui render threw exception", ex);
            return false;
        }
    }

    private boolean safeClose(UUID sessionId, GuiCloseReason reason) {
        try {
            return guiPort.close(sessionId, reason);
        } catch (RuntimeException ex) {
            logger.error("gui close threw exception", ex);
            return false;
        }
    }

    private static GuiEvent toLegacyEvent(GuiInteractionEvent event) {
        if (event instanceof SlotClickEvent slotClickEvent) {
            return new GuiClickEvent(
                    slotClickEvent.sessionId(),
                    slotClickEvent.expectedRevision(),
                    slotClickEvent.action(),
                    slotClickEvent.slot()
            );
        }
        if (event instanceof CloseEventPortable closeEventPortable) {
            return new GuiCloseEvent(
                    closeEventPortable.sessionId(),
                    closeEventPortable.expectedRevision(),
                    closeEventPortable.reason()
            );
        }
        if (event instanceof TimeoutEventPortable timeoutEventPortable) {
            return new GuiTimeoutEvent(timeoutEventPortable.sessionId(), timeoutEventPortable.expectedRevision());
        }
        if (event instanceof DisconnectEventPortable disconnectEventPortable) {
            return new GuiDisconnectEvent(
                    disconnectEventPortable.sessionId(),
                    disconnectEventPortable.expectedRevision(),
                    disconnectEventPortable.playerId()
            );
        }
        return null;
    }

    private static GuiEventResult toLegacyResult(GuiInteractionResult result) {
        return switch (result) {
            case APPLIED -> GuiEventResult.APPLIED;
            case DENIED_BY_POLICY -> GuiEventResult.DENIED_BY_POLICY;
            case STALE_REVISION -> GuiEventResult.STALE_REVISION;
            case SESSION_NOT_FOUND -> GuiEventResult.SESSION_NOT_FOUND;
            case SESSION_NOT_OPEN, INVALID_ACTION -> GuiEventResult.SESSION_NOT_OPEN;
        };
    }

    private static final class SessionRecord {

        private final UUID sessionId;
        private final UUID playerId;
        private final GuiOpenOptions options;
        private final long openedAtEpochMilli;
        private final long timeoutTicks;
        private final Deque<NavigationFrame> navigationStack;

        private GuiDefinition definition;
        private Map<String, String> placeholders;
        private Locale locale;
        private GuiState state;
        private GuiSessionStatus status;
        private long lastInteractionEpochMilli;
        private TaskHandle timeoutHandle;

        private SessionRecord(UUID sessionId, UUID playerId, GuiDefinition definition, GuiOpenOptions options, long now) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.playerId = Objects.requireNonNull(playerId, "playerId");
            this.definition = Objects.requireNonNull(definition, "definition");
            this.options = Objects.requireNonNull(options, "options");
            this.timeoutTicks = options.timeoutTicks();
            this.state = GuiState.empty();
            this.placeholders = Map.copyOf(options.placeholders());
            this.locale = options.locale();
            this.status = GuiSessionStatus.OPEN;
            this.openedAtEpochMilli = now;
            this.lastInteractionEpochMilli = now;
            this.navigationStack = new ArrayDeque<>();
        }

        private synchronized UUID sessionId() {
            return sessionId;
        }

        private synchronized UUID playerId() {
            return playerId;
        }

        private synchronized GuiDefinition definition() {
            return definition;
        }

        private synchronized GuiOpenOptions options() {
            return options;
        }

        private synchronized Map<String, String> placeholders() {
            return Map.copyOf(placeholders);
        }

        private synchronized Locale locale() {
            return locale;
        }

        private synchronized GuiState state() {
            return state;
        }

        private synchronized boolean belongsTo(UUID playerId) {
            return this.playerId.equals(playerId);
        }

        private synchronized boolean isOpen() {
            return status == GuiSessionStatus.OPEN;
        }

        private synchronized GuiSession snapshot() {
            return new GuiSession(
                    sessionId,
                    playerId,
                    definition.key(),
                    state,
                    status,
                    openedAtEpochMilli,
                    lastInteractionEpochMilli,
                    timeoutTicks
            );
        }

        private synchronized GuiSession snapshotClosed() {
            return new GuiSession(
                    sessionId,
                    playerId,
                    definition.key(),
                    state,
                    GuiSessionStatus.CLOSED,
                    openedAtEpochMilli,
                    lastInteractionEpochMilli,
                    timeoutTicks
            );
        }

        private synchronized void setTimeoutHandle(TaskHandle timeoutHandle) {
            this.timeoutHandle = timeoutHandle;
        }

        private synchronized void cancelTimeout() {
            if (timeoutHandle != null) {
                timeoutHandle.cancel();
            }
        }

        private synchronized GuiUpdateResult update(GuiState nextState, long expectedRevision) {
            if (status != GuiSessionStatus.OPEN) {
                return GuiUpdateResult.SESSION_NOT_OPEN;
            }
            if (state.revision() != expectedRevision) {
                return GuiUpdateResult.STALE_REVISION;
            }

            state = GuiState.withData(expectedRevision + 1L, nextState.data());
            lastInteractionEpochMilli = System.currentTimeMillis();
            return GuiUpdateResult.APPLIED;
        }

        private synchronized void replaceState(Map<String, String> data) {
            state = GuiState.withData(state.revision() + 1L, data);
            lastInteractionEpochMilli = System.currentTimeMillis();
        }

        private synchronized void applyToggle(int slot, String stateKey, String value, GuiItemView nextItem) {
            Map<String, String> nextData = new HashMap<>(state.data());
            nextData.put(stateKey, value);
            state = GuiState.withData(state.revision() + 1L, nextData);

            Map<Integer, SlotDefinition> nextSlots = new HashMap<>(definition.slots());
            SlotDefinition previous = nextSlots.get(slot);
            if (previous != null) {
                nextSlots.put(slot, new SlotDefinition(slot, nextItem, previous.interaction(), previous.actions()));
                definition = new GuiDefinition(
                        definition.key(),
                        definition.layout(),
                        definition.title(),
                        nextSlots,
                        definition.behaviorPolicy()
                );
            }
            lastInteractionEpochMilli = System.currentTimeMillis();
        }

        private synchronized NavigationFrame captureFrame() {
            return new NavigationFrame(definition, state, placeholders, locale);
        }

        private synchronized void applyFrame(NavigationFrame frame) {
            this.definition = frame.definition();
            this.state = frame.state();
            this.placeholders = Map.copyOf(frame.placeholders());
            this.locale = frame.locale();
            this.lastInteractionEpochMilli = System.currentTimeMillis();
        }

        private synchronized void pushNavigation(NavigationFrame frame, int limit) {
            if (navigationStack.size() >= limit) {
                navigationStack.removeFirst();
            }
            navigationStack.addLast(frame);
        }

        private synchronized int navigationSize() {
            return navigationStack.size();
        }

        private synchronized Optional<NavigationFrame> popNavigation() {
            if (navigationStack.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(navigationStack.removeLast());
        }

        private synchronized GuiInteractionResult precheck(long expectedRevision) {
            if (status != GuiSessionStatus.OPEN) {
                return GuiInteractionResult.SESSION_NOT_OPEN;
            }
            if (state.revision() != expectedRevision) {
                return GuiInteractionResult.STALE_REVISION;
            }
            return GuiInteractionResult.APPLIED;
        }

        private synchronized void touch() {
            if (status == GuiSessionStatus.OPEN) {
                lastInteractionEpochMilli = System.currentTimeMillis();
            }
        }

        private synchronized boolean beginClosing() {
            if (status != GuiSessionStatus.OPEN) {
                return false;
            }
            status = GuiSessionStatus.CLOSING;
            return true;
        }

        private synchronized void finishClosing(GuiCloseReason reason) {
            status = reason == GuiCloseReason.TIMEOUT ? GuiSessionStatus.TIMED_OUT : GuiSessionStatus.CLOSED;
            lastInteractionEpochMilli = System.currentTimeMillis();
        }

        private record NavigationFrame(
                GuiDefinition definition,
                GuiState state,
                Map<String, String> placeholders,
                Locale locale
        ) {
        }
    }
}
