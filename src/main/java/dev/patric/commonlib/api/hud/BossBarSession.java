package dev.patric.commonlib.api.hud;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable bossbar session snapshot.
 *
 * @param barId bar id.
 * @param playerId player id.
 * @param barKey logical bar key.
 * @param state current state.
 * @param closed close flag.
 * @param openedAtEpochMilli open timestamp.
 * @param lastRenderedTick last render tick counter.
 */
public record BossBarSession(
        UUID barId,
        UUID playerId,
        String barKey,
        BossBarState state,
        boolean closed,
        long openedAtEpochMilli,
        long lastRenderedTick
) {

    /**
     * Compact constructor validation.
     */
    public BossBarSession {
        barId = Objects.requireNonNull(barId, "barId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        barKey = Objects.requireNonNull(barKey, "barKey").trim();
        if (barKey.isEmpty()) {
            throw new IllegalArgumentException("barKey must not be blank");
        }
        state = Objects.requireNonNull(state, "state");
        if (openedAtEpochMilli < 0L) {
            throw new IllegalArgumentException("openedAtEpochMilli must be >= 0");
        }
        if (lastRenderedTick < -1L) {
            throw new IllegalArgumentException("lastRenderedTick must be >= -1");
        }
    }
}
