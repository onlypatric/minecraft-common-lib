package dev.patric.commonlib.api.dialog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * High-level service for Paper dialog sessions.
 */
public interface DialogService {

    /**
     * Opens a dialog session.
     *
     * @param request open request.
     * @return opened session snapshot.
     */
    DialogSession open(DialogOpenRequest request);

    /**
     * Finds session by id.
     *
     * @param sessionId session id.
     * @return optional session snapshot.
     */
    Optional<DialogSession> find(UUID sessionId);

    /**
     * Returns active sessions for a player.
     *
     * @param playerId player id.
     * @return active sessions.
     */
    List<DialogSession> activeByPlayer(UUID playerId);

    /**
     * Publishes a portable dialog event routed through policies.
     *
     * @param event dialog event.
     * @return event result.
     */
    DialogEventResult publish(DialogEvent event);

    /**
     * Closes one session.
     *
     * @param sessionId session id.
     * @param reason close reason.
     * @return true if this call closed the session.
     */
    boolean close(UUID sessionId, DialogCloseReason reason);

    /**
     * Closes all sessions for a specific player.
     *
     * @param playerId player id.
     * @param reason close reason.
     * @return number of closed sessions.
     */
    int closeAllByPlayer(UUID playerId, DialogCloseReason reason);

    /**
     * Closes all active sessions.
     *
     * @param reason close reason.
     * @return number of closed sessions.
     */
    int closeAll(DialogCloseReason reason);
}
