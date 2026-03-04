package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.ScoreboardPort;
import java.util.Objects;
import java.util.UUID;

/**
 * No-op scoreboard port.
 */
public final class NoopScoreboardPort implements ScoreboardPort {

    @Override
    public boolean open(ScoreboardSession session) {
        Objects.requireNonNull(session, "session");
        return true;
    }

    @Override
    public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(snapshot, "snapshot");
        return true;
    }

    @Override
    public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        return true;
    }
}
