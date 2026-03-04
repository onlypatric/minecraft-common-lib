package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import java.util.UUID;

/**
 * Adapter-facing scoreboard rendering port.
 */
public interface ScoreboardPort {

    /**
     * Opens backend scoreboard representation for a session.
     *
     * @param session session snapshot.
     * @return true if backend accepted open.
     */
    boolean open(ScoreboardSession session);

    /**
     * Renders a scoreboard snapshot for the session.
     *
     * @param sessionId session id.
     * @param snapshot scoreboard snapshot.
     * @return true if backend accepted render.
     */
    boolean render(UUID sessionId, ScoreboardSnapshot snapshot);

    /**
     * Closes backend scoreboard representation.
     *
     * @param sessionId session id.
     * @param reason close reason.
     * @return true if backend accepted close.
     */
    boolean close(UUID sessionId, HudAudienceCloseReason reason);
}
