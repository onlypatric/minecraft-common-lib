package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiPortFeature;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import java.util.UUID;

/**
 * Adapter-facing GUI rendering port.
 */
public interface GuiPort {

    /**
     * Opens backend view for a render model.
     *
     * @param renderModel full render model.
     * @return true if backend accepted the open operation.
     */
    boolean open(GuiRenderModel renderModel);

    /**
     * Renders a state patch on an existing backend session.
     *
     * @param sessionId session id.
     * @param patch render patch.
     * @return true when backend accepted render request.
     */
    boolean render(UUID sessionId, GuiRenderPatch patch);

    /**
     * Closes backend session representation.
     *
     * @param sessionId session id.
     * @param reason close reason.
     * @return true when backend accepted close request.
     */
    boolean close(UUID sessionId, GuiCloseReason reason);

    /**
     * Indicates whether one backend feature is supported.
     *
     * @param feature feature key.
     * @return true if supported.
     */
    boolean supports(GuiPortFeature feature);

    /**
     * Legacy open bridge.
     *
     * @param session legacy session snapshot.
     * @return true if open operation was accepted.
     */
    @Deprecated
    default boolean open(GuiSession session) {
        return open(new GuiRenderModel(session.sessionId(), session.playerId(), GuiDefinition.chest(
                session.viewKey(),
                6,
                session.viewKey()
        ), session.state()));
    }

    /**
     * Legacy render bridge.
     *
     * @param sessionId session id.
     * @param state state.
     * @return true if render operation was accepted.
     */
    @Deprecated
    default boolean render(UUID sessionId, GuiState state) {
        return render(sessionId, new GuiRenderPatch(sessionId, state));
    }

    /**
     * Legacy feature probe bridge.
     *
     * @return true if click forwarding is supported.
     */
    @Deprecated
    default boolean supportsPortableEvents() {
        return supports(GuiPortFeature.CLICK);
    }
}
