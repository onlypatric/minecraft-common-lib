package dev.patric.commonlib.api.hud;

import java.util.Objects;
import java.util.UUID;

/**
 * Request payload for opening bossbar sessions.
 *
 * @param playerId player id.
 * @param barKey logical bar key.
 * @param initialState initial state.
 */
public record BossBarOpenRequest(UUID playerId, String barKey, BossBarState initialState) {

    /**
     * Compact constructor validation.
     */
    public BossBarOpenRequest {
        playerId = Objects.requireNonNull(playerId, "playerId");
        barKey = Objects.requireNonNull(barKey, "barKey").trim();
        if (barKey.isEmpty()) {
            throw new IllegalArgumentException("barKey must not be blank");
        }
        initialState = Objects.requireNonNull(initialState, "initialState");
    }
}
