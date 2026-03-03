package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot of a GUI session.
 *
 * @param sessionId session id.
 * @param playerId owning player id.
 * @param viewKey logical view key.
 * @param state current state snapshot.
 * @param status current lifecycle status.
 * @param openedAtEpochMilli open timestamp in epoch millis.
 * @param lastInteractionEpochMilli last interaction timestamp in epoch millis.
 * @param timeoutTicks configured timeout in ticks.
 */
public record GuiSession(
        UUID sessionId,
        UUID playerId,
        String viewKey,
        GuiState state,
        GuiSessionStatus status,
        long openedAtEpochMilli,
        long lastInteractionEpochMilli,
        long timeoutTicks
) {

    /**
     * Compact constructor validation.
     */
    public GuiSession {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        viewKey = Objects.requireNonNull(viewKey, "viewKey").trim();
        if (viewKey.isEmpty()) {
            throw new IllegalArgumentException("viewKey must not be blank");
        }
        state = Objects.requireNonNull(state, "state");
        status = Objects.requireNonNull(status, "status");
        if (openedAtEpochMilli < 0L || lastInteractionEpochMilli < 0L) {
            throw new IllegalArgumentException("timestamps must be >= 0");
        }
        if (timeoutTicks < 0L) {
            throw new IllegalArgumentException("timeoutTicks must be >= 0");
        }
    }
}
