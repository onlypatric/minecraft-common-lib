package dev.patric.commonlib.api.match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic match/state engine service.
 */
public interface MatchEngineService {

    /**
     * Opens a new match session.
     */
    MatchSession open(MatchOpenRequest request);

    /**
     * Finds a match session by id.
     */
    Optional<MatchSession> find(UUID matchId);

    /**
     * Returns active match sessions.
     */
    List<MatchSession> active();

    /**
     * Starts countdown from lobby.
     */
    MatchTransitionResult startCountdown(UUID matchId);

    /**
     * Applies state transition.
     */
    MatchTransitionResult transition(UUID matchId, MatchState target, EndReason reason);

    /**
     * Starts ending path with explicit reason.
     */
    MatchTransitionResult end(UUID matchId, EndReason reason);

    /**
     * Adds a player as connected participant.
     */
    JoinResult join(UUID matchId, UUID playerId);

    /**
     * Marks a player disconnected.
     */
    DisconnectResult disconnect(UUID matchId, UUID playerId);

    /**
     * Attempts player rejoin according to policy.
     */
    RejoinResult rejoin(UUID matchId, UUID playerId);

    /**
     * Closes all active matches.
     */
    int closeAll(EndReason reason);

    /**
     * Handles player quit events.
     */
    void onPlayerQuit(UUID playerId);

    /**
     * Handles player world-change events.
     */
    void onPlayerWorldChange(UUID playerId);

    /**
     * Indicates whether engine loop is idle.
     */
    boolean isIdle();
}
