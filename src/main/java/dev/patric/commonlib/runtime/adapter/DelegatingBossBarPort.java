package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.port.BossBarPort;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bossbar port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingBossBarPort implements BossBarPort {

    private final BossBarPort fallback;
    private final AtomicReference<BossBarPort> delegate;

    /**
     * Creates a delegating bossbar port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingBossBarPort(BossBarPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(BossBarPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public boolean open(BossBarSession session) {
        return delegate.get().open(session);
    }

    @Override
    public boolean render(UUID barId, BossBarState state) {
        return delegate.get().render(barId, state);
    }

    @Override
    public boolean close(UUID barId, HudAudienceCloseReason reason) {
        return delegate.get().close(barId, reason);
    }
}
