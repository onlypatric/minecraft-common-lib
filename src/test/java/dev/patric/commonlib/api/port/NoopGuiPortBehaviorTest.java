package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiPortFeature;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import dev.patric.commonlib.api.port.noop.NoopGuiPort;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoopGuiPortBehaviorTest {

    @Test
    void noopGuiPortReturnsDeterministicSafeValues() {
        NoopGuiPort port = new NoopGuiPort();
        UUID playerId = UUID.randomUUID();
        GuiOpenRequest request = new GuiOpenRequest(playerId, "menu.main", GuiState.withData(0L, Map.of("page", "1")), 20L);
        GuiSession session = new GuiSession(
                UUID.randomUUID(),
                playerId,
                request.viewKey(),
                request.initialState(),
                dev.patric.commonlib.api.gui.GuiSessionStatus.OPEN,
                1L,
                1L,
                request.timeoutTicks()
        );

        assertTrue(port.open(session));
        assertTrue(port.render(session.sessionId(), GuiState.withData(1L, Map.of("page", "2"))));
        assertTrue(port.open(new GuiRenderModel(
                session.sessionId(),
                session.playerId(),
                GuiDefinition.chest("menu.main", 6, "Main"),
                session.state()
        )));
        assertTrue(port.render(session.sessionId(), new GuiRenderPatch(session.sessionId(), GuiState.empty())));
        assertTrue(port.close(session.sessionId(), GuiCloseReason.USER_CLOSE));
        assertFalse(port.supportsPortableEvents());
        assertFalse(port.supports(GuiPortFeature.CLICK));
    }
}
