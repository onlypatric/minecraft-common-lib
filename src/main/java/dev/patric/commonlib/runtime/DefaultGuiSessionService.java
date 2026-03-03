package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.gui.GuiCloseEvent;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDisconnectEvent;
import dev.patric.commonlib.api.gui.GuiEvent;
import dev.patric.commonlib.api.gui.GuiEventResult;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.GuiSessionStatus;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.GuiTimeoutEvent;
import dev.patric.commonlib.api.gui.GuiUpdateResult;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.lifecycle.gui.GuiPolicyRoutedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default thread-safe GUI session service.
 */
public final class DefaultGuiSessionService implements GuiSessionService {

    private final Map<UUID, SessionRecord> sessions;
    private final CommonScheduler scheduler;
    private final EventRouter eventRouter;
    private final RuntimeLogger logger;
    private final GuiPort guiPort;

    /**
     * Creates a GUI session service bound to runtime services.
     *
     * @param scheduler scheduler facade.
     * @param eventRouter policy router.
     * @param logger runtime logger.
     * @param guiPort backend gui port.
     */
    public DefaultGuiSessionService(
            CommonScheduler scheduler,
            EventRouter eventRouter,
            RuntimeLogger logger,
            GuiPort guiPort
    ) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.eventRouter = Objects.requireNonNull(eventRouter, "eventRouter");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.guiPort = Objects.requireNonNull(guiPort, "guiPort");
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public GuiSession open(GuiOpenRequest request) {
        Objects.requireNonNull(request, "request");

        UUID sessionId = UUID.randomUUID();
        long now = System.currentTimeMillis();
        SessionRecord record = new SessionRecord(sessionId, request, now);
        sessions.put(sessionId, record);

        if (request.timeoutTicks() > 0L) {
            TaskHandle timeoutHandle = scheduler.runSyncLater(request.timeoutTicks(), () -> {
                Optional<GuiSession> snapshot = find(sessionId);
                if (snapshot.isEmpty()) {
                    return;
                }
                publish(new GuiTimeoutEvent(sessionId, snapshot.get().state().revision()));
            });
            record.setTimeoutHandle(timeoutHandle);
        }

        boolean opened = safeOpen(record.snapshot());
        if (!opened) {
            logger.warn("gui open rejected by backend: " + request.viewKey() + " for " + request.playerId());
            closeInternal(sessionId, GuiCloseReason.ERROR);
            return record.snapshotClosed();
        }

        return record.snapshot();
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
    public GuiEventResult publish(GuiEvent event) {
        Objects.requireNonNull(event, "event");

        SessionRecord record = sessions.get(event.sessionId());
        if (record == null) {
            return GuiEventResult.SESSION_NOT_FOUND;
        }

        GuiPolicyRoutedEvent routedEvent = new GuiPolicyRoutedEvent(event);
        PolicyDecision decision = eventRouter.route(routedEvent);
        if (decision.denied()) {
            return GuiEventResult.DENIED_BY_POLICY;
        }

        if (event instanceof GuiDisconnectEvent disconnect && !record.belongsTo(disconnect.playerId())) {
            return GuiEventResult.SESSION_NOT_FOUND;
        }

        GuiEventResult precheck = record.precheck(event.expectedRevision());
        if (precheck != GuiEventResult.APPLIED) {
            return precheck;
        }

        if (event instanceof GuiCloseEvent closeEvent) {
            return closeInternal(closeEvent.sessionId(), closeEvent.reason())
                    ? GuiEventResult.APPLIED
                    : GuiEventResult.SESSION_NOT_OPEN;
        }
        if (event instanceof GuiTimeoutEvent timeoutEvent) {
            return closeInternal(timeoutEvent.sessionId(), GuiCloseReason.TIMEOUT)
                    ? GuiEventResult.APPLIED
                    : GuiEventResult.SESSION_NOT_OPEN;
        }
        if (event instanceof GuiDisconnectEvent disconnectEvent) {
            return closeInternal(disconnectEvent.sessionId(), GuiCloseReason.DISCONNECT)
                    ? GuiEventResult.APPLIED
                    : GuiEventResult.SESSION_NOT_OPEN;
        }

        record.touch();
        return GuiEventResult.APPLIED;
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

    private boolean safeOpen(GuiSession session) {
        try {
            return guiPort.open(session);
        } catch (RuntimeException ex) {
            logger.error("gui open threw exception", ex);
            return false;
        }
    }

    private boolean safeRender(UUID sessionId, GuiState state) {
        try {
            return guiPort.render(sessionId, state);
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

    private static final class SessionRecord {

        private final UUID sessionId;
        private final UUID playerId;
        private final String viewKey;
        private final long openedAtEpochMilli;
        private final long timeoutTicks;

        private GuiState state;
        private GuiSessionStatus status;
        private long lastInteractionEpochMilli;
        private TaskHandle timeoutHandle;

        private SessionRecord(UUID sessionId, GuiOpenRequest request, long now) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.playerId = request.playerId();
            this.viewKey = request.viewKey();
            this.timeoutTicks = request.timeoutTicks();
            this.state = request.initialState();
            this.status = GuiSessionStatus.OPEN;
            this.openedAtEpochMilli = now;
            this.lastInteractionEpochMilli = now;
        }

        private synchronized UUID sessionId() {
            return sessionId;
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
                    viewKey,
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
                    viewKey,
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

        private synchronized GuiEventResult precheck(long expectedRevision) {
            if (status != GuiSessionStatus.OPEN) {
                return GuiEventResult.SESSION_NOT_OPEN;
            }
            if (state.revision() != expectedRevision) {
                return GuiEventResult.STALE_REVISION;
            }
            return GuiEventResult.APPLIED;
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
    }
}
