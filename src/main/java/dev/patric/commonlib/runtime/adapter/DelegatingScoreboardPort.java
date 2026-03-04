package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.ScoreboardPort;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Scoreboard port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingScoreboardPort implements ScoreboardPort {

    private final ScoreboardPort fallback;
    private final AtomicReference<ScoreboardPort> delegate;

    /**
     * Creates a delegating scoreboard port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingScoreboardPort(ScoreboardPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(ScoreboardPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public boolean open(ScoreboardSession session) {
        return delegate.get().open(session);
    }

    @Override
    public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
        return delegate.get().render(sessionId, snapshot);
    }

    @Override
    public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
        return delegate.get().close(sessionId, reason);
    }
}
