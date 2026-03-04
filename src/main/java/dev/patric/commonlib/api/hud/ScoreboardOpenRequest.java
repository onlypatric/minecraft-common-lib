package dev.patric.commonlib.api.hud;

import java.util.Objects;
import java.util.UUID;

/**
 * Request payload for opening scoreboard sessions.
 *
 * @param playerId player id.
 * @param boardKey logical board key.
 * @param initialSnapshot initial payload.
 */
public record ScoreboardOpenRequest(UUID playerId, String boardKey, ScoreboardSnapshot initialSnapshot) {

    /**
     * Compact constructor validation.
     */
    public ScoreboardOpenRequest {
        playerId = Objects.requireNonNull(playerId, "playerId");
        boardKey = Objects.requireNonNull(boardKey, "boardKey").trim();
        if (boardKey.isEmpty()) {
            throw new IllegalArgumentException("boardKey must not be blank");
        }
        initialSnapshot = Objects.requireNonNull(initialSnapshot, "initialSnapshot");
    }
}
