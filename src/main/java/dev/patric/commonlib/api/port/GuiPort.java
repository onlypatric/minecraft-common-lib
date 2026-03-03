package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiState;
import java.util.UUID;

/**
 * Adapter-facing GUI rendering port.
 */
public interface GuiPort {

    /**
     * Opens the backend GUI representation for the provided session.
     *
     * @param session gui session snapshot.
     * @return true if backend accepted the open operation.
     */
    boolean open(GuiSession session);

    /**
     * Renders a state update on an existing backend session.
     *
     * @param sessionId session id.
     * @param state next state.
     * @return true when backend accepted render request.
     */
    boolean render(UUID sessionId, GuiState state);

    /**
     * Closes backend session representation.
     *
     * @param sessionId session id.
     * @param reason close reason.
     * @return true when backend accepted close request.
     */
    boolean close(UUID sessionId, GuiCloseReason reason);

    /**
     * Indicates if adapter can forward portable GUI events.
     *
     * @return true if supported.
     */
    boolean supportsPortableEvents();
}
