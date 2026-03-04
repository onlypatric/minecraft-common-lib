package dev.patric.commonlib.api.gui.render;

import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiState;
import java.util.Objects;
import java.util.UUID;

/**
 * Full render payload passed to GUI backend adapters.
 *
 * @param sessionId session id.
 * @param playerId player id.
 * @param definition GUI definition.
 * @param state current state.
 */
public record GuiRenderModel(
        UUID sessionId,
        UUID playerId,
        GuiDefinition definition,
        GuiState state
) {

    /**
     * Compact constructor validation.
     */
    public GuiRenderModel {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        definition = Objects.requireNonNull(definition, "definition");
        state = Objects.requireNonNull(state, "state");
    }
}
