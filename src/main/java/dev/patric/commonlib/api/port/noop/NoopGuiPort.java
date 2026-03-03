package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.port.GuiPort;
import java.util.Objects;
import java.util.UUID;

/**
 * No-op GUI port.
 */
public final class NoopGuiPort implements GuiPort {

    @Override
    public boolean open(GuiSession session) {
        Objects.requireNonNull(session, "session");
        return true;
    }

    @Override
    public boolean render(UUID sessionId, GuiState state) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(state, "state");
        return true;
    }

    @Override
    public boolean close(UUID sessionId, GuiCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        return true;
    }

    @Override
    public boolean supportsPortableEvents() {
        return false;
    }
}
