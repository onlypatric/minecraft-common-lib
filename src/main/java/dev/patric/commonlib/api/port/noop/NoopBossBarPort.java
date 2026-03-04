package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.port.BossBarPort;
import java.util.Objects;
import java.util.UUID;

/**
 * No-op bossbar port.
 */
public final class NoopBossBarPort implements BossBarPort {

    @Override
    public boolean open(BossBarSession session) {
        Objects.requireNonNull(session, "session");
        return true;
    }

    @Override
    public boolean render(UUID barId, BossBarState state) {
        Objects.requireNonNull(barId, "barId");
        Objects.requireNonNull(state, "state");
        return true;
    }

    @Override
    public boolean close(UUID barId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(barId, "barId");
        Objects.requireNonNull(reason, "reason");
        return true;
    }
}
