package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Request payload used when opening a GUI session.
 *
 * @param playerId target player.
 * @param viewKey logical view key.
 * @param initialState initial GUI state.
 * @param timeoutTicks timeout in ticks, 0 disables timeout.
 */
public record GuiOpenRequest(UUID playerId, String viewKey, GuiState initialState, long timeoutTicks) {

    /**
     * Compact constructor validation.
     */
    public GuiOpenRequest {
        playerId = Objects.requireNonNull(playerId, "playerId");
        viewKey = Objects.requireNonNull(viewKey, "viewKey").trim();
        if (viewKey.isEmpty()) {
            throw new IllegalArgumentException("viewKey must not be blank");
        }
        initialState = initialState == null ? GuiState.empty() : initialState;
        if (timeoutTicks < 0L) {
            throw new IllegalArgumentException("timeoutTicks must be >= 0");
        }
    }
}
