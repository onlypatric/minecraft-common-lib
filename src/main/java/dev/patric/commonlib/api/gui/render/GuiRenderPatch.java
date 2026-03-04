package dev.patric.commonlib.api.gui.render;

import dev.patric.commonlib.api.gui.GuiState;
import java.util.Objects;
import java.util.UUID;

/**
 * Render update payload passed to GUI backend adapters.
 *
 * @param sessionId session id.
 * @param state target state.
 */
public record GuiRenderPatch(
        UUID sessionId,
        GuiState state
) {

    /**
     * Compact constructor validation.
     */
    public GuiRenderPatch {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        state = Objects.requireNonNull(state, "state");
    }
}
