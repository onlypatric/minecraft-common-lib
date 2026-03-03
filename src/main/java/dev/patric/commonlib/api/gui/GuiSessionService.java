package dev.patric.commonlib.api.gui;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Session-oriented GUI service abstraction.
 */
public interface GuiSessionService {

    /**
     * Opens a GUI session.
     *
     * @param request open request.
     * @return opened session snapshot.
     */
    GuiSession open(GuiOpenRequest request);

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
     * Publishes a portable GUI event through policy hooks.
     *
     * @param event gui event.
     * @return event processing result.
     */
    GuiEventResult publish(GuiEvent event);

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
