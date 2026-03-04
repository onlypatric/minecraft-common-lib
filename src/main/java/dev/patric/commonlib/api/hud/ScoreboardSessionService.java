package dev.patric.commonlib.api.hud;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Session-oriented scoreboard abstraction.
 */
public interface ScoreboardSessionService {

    /**
     * Opens a scoreboard session.
     *
     * @param request open request.
     * @return opened session snapshot.
     */
    ScoreboardSession open(ScoreboardOpenRequest request);

    /**
     * Finds session by id.
     *
     * @param sessionId session id.
     * @return optional session snapshot.
     */
    Optional<ScoreboardSession> find(UUID sessionId);

    /**
     * Returns active sessions for a player.
     *
     * @param playerId player id.
     * @return session snapshots.
     */
    List<ScoreboardSession> activeByPlayer(UUID playerId);

    /**
     * Applies scoreboard update with policy throttling/deduping.
     *
     * @param sessionId session id.
     * @param nextSnapshot target snapshot.
     * @return update result.
     */
    ScoreboardUpdateResult update(UUID sessionId, ScoreboardSnapshot nextSnapshot);

    /**
     * Closes session.
     *
     * @param sessionId session id.
     * @param reason close reason.
     * @return true if closed by this call.
     */
    boolean close(UUID sessionId, HudAudienceCloseReason reason);

    /**
     * Closes all sessions for a player.
     *
     * @param playerId player id.
     * @param reason close reason.
     * @return closed count.
     */
    int closeAllByPlayer(UUID playerId, HudAudienceCloseReason reason);

    /**
     * Closes all sessions.
     *
     * @param reason close reason.
     * @return closed count.
     */
    int closeAll(HudAudienceCloseReason reason);

    /**
     * Audience quit cleanup hook.
     *
     * @param playerId player id.
     */
    void onPlayerQuit(UUID playerId);

    /**
     * Audience world-change cleanup hook.
     *
     * @param playerId player id.
     */
    void onPlayerWorldChange(UUID playerId);

    /**
     * Returns active update policy.
     *
     * @return policy.
     */
    HudUpdatePolicy policy();
}
