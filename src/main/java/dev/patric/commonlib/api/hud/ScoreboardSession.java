package dev.patric.commonlib.api.hud;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable scoreboard session snapshot.
 *
 * @param sessionId session id.
 * @param playerId player id.
 * @param boardKey logical board key.
 * @param snapshot current rendered snapshot.
 * @param status session status.
 * @param openedAtEpochMilli open timestamp.
 * @param lastRenderedTick last render tick counter.
 */
public record ScoreboardSession(
        UUID sessionId,
        UUID playerId,
        String boardKey,
        ScoreboardSnapshot snapshot,
        ScoreboardSessionStatus status,
        long openedAtEpochMilli,
        long lastRenderedTick
) {

    /**
     * Compact constructor validation.
     */
    public ScoreboardSession {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        boardKey = Objects.requireNonNull(boardKey, "boardKey").trim();
        if (boardKey.isEmpty()) {
            throw new IllegalArgumentException("boardKey must not be blank");
        }
        snapshot = Objects.requireNonNull(snapshot, "snapshot");
        status = Objects.requireNonNull(status, "status");
        if (openedAtEpochMilli < 0L) {
            throw new IllegalArgumentException("openedAtEpochMilli must be >= 0");
        }
        if (lastRenderedTick < -1L) {
            throw new IllegalArgumentException("lastRenderedTick must be >= -1");
        }
    }
}
