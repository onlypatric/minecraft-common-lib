package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiPortFeature;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import dev.patric.commonlib.api.port.GuiPort;
import java.util.Objects;
import java.util.UUID;

/**
 * No-op GUI port.
 */
public final class NoopGuiPort implements GuiPort {

    @Override
    public boolean open(GuiRenderModel renderModel) {
        Objects.requireNonNull(renderModel, "renderModel");
        return true;
    }

    @Override
    public boolean render(UUID sessionId, GuiRenderPatch patch) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(patch, "patch");
        return true;
    }

    @Override
    public boolean close(UUID sessionId, GuiCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        return true;
    }

    @Override
    public boolean supports(GuiPortFeature feature) {
        Objects.requireNonNull(feature, "feature");
        return false;
    }
}
