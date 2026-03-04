package dev.patric.commonlib.api.gui;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Session-oriented GUI service abstraction.
 */
public interface GuiSessionService {

    /**
     * Opens a GUI session from a definition and options.
     *
     * @param definition GUI definition.
     * @param playerId player id.
     * @param options open options.
     * @return opened session snapshot.
     */
    GuiSession open(GuiDefinition definition, UUID playerId, GuiOpenOptions options);

    /**
     * Legacy open request bridge.
     *
     * @param request legacy request.
     * @return opened session.
     */
    @Deprecated
    default GuiSession open(GuiOpenRequest request) {
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
                java.util.Locale.ENGLISH,
                Map.of()
        );
        GuiSession session = open(definition, request.playerId(), options);
        if (!request.initialState().data().isEmpty()) {
            update(session.sessionId(), request.initialState(), session.state().revision());
        }
        return find(session.sessionId()).orElse(session);
    }

    /**
     * Looks up a session by id.
     *
     * @param sessionId session id.
     * @return optional snapshot.
     */
    Optional<GuiSession> find(UUID sessionId);

    /**
     * Lists active sessions for a player.
     *
     * @param playerId player id.
     * @return active session snapshots.
     */
    List<GuiSession> activeByPlayer(UUID playerId);

    /**
     * Updates session state with optimistic revision guard.
     *
     * @param sessionId session id.
     * @param nextState target state payload.
     * @param expectedRevision expected current revision.
     * @return update result.
     */
    GuiUpdateResult update(UUID sessionId, GuiState nextState, long expectedRevision);

    /**
     * Executes one interaction event through policy hooks.
     *
     * @param event interaction event.
     * @return event processing result.
     */
    GuiInteractionResult interact(GuiInteractionEvent event);

    /**
     * Legacy GUI event bridge.
     *
     * @param event legacy event.
     * @return legacy result.
     */
    @Deprecated
    default GuiEventResult publish(GuiEvent event) {
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

        return switch (interact(interactionEvent)) {
            case APPLIED -> GuiEventResult.APPLIED;
            case DENIED_BY_POLICY -> GuiEventResult.DENIED_BY_POLICY;
            case STALE_REVISION -> GuiEventResult.STALE_REVISION;
            case SESSION_NOT_FOUND -> GuiEventResult.SESSION_NOT_FOUND;
            case SESSION_NOT_OPEN, INVALID_ACTION -> GuiEventResult.SESSION_NOT_OPEN;
        };
    }

    /**
     * Closes a specific session.
     *
     * @param sessionId session id.
     * @param reason close reason.
     * @return true if closed by this call.
     */
    boolean close(UUID sessionId, GuiCloseReason reason);

    /**
     * Closes all sessions belonging to a player.
     *
     * @param playerId player id.
     * @param reason close reason.
     * @return number of closed sessions.
     */
    int closeAllByPlayer(UUID playerId, GuiCloseReason reason);

    /**
     * Closes all active sessions.
     *
     * @param reason close reason.
     * @return number of closed sessions.
     */
    int closeAll(GuiCloseReason reason);
}
