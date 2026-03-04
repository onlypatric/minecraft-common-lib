package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import java.util.UUID;

/**
 * Adapter-facing bossbar rendering port.
 */
public interface BossBarPort {

    /**
     * Opens backend bossbar for a session.
     *
     * @param session session snapshot.
     * @return true if backend accepted open.
     */
    boolean open(BossBarSession session);

    /**
     * Renders bossbar state update.
     *
     * @param barId bar id.
     * @param state next state.
     * @return true if backend accepted render.
     */
    boolean render(UUID barId, BossBarState state);

    /**
     * Closes backend bossbar.
     *
     * @param barId bar id.
     * @param reason close reason.
     * @return true if backend accepted close.
     */
    boolean close(UUID barId, HudAudienceCloseReason reason);
}
