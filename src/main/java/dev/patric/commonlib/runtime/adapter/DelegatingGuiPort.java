package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiPortFeature;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import dev.patric.commonlib.api.port.GuiPort;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * GUI port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingGuiPort implements GuiPort {

    private final GuiPort fallback;
    private final AtomicReference<GuiPort> delegate;

    /**
     * Creates a delegating GUI port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingGuiPort(GuiPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(GuiPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public boolean open(GuiRenderModel renderModel) {
        return delegate.get().open(renderModel);
    }

    @Override
    public boolean render(UUID sessionId, GuiRenderPatch patch) {
        return delegate.get().render(sessionId, patch);
    }

    @Override
    public boolean close(UUID sessionId, GuiCloseReason reason) {
        return delegate.get().close(sessionId, reason);
    }

    @Override
    public boolean supports(GuiPortFeature feature) {
        return delegate.get().supports(feature);
    }
}
