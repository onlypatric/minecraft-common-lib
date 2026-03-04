package dev.patric.commonlib.adapter.invui;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvUiInteractionForwardingTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void forwardsOpenRenderCloseToInjectedBackend() {
        PlayerMock player = server.addPlayer();
        UUID sessionId = UUID.randomUUID();
        TrackingBackend backend = new TrackingBackend();
        InvUiGuiPort port = new InvUiGuiPort(backend);

        GuiDefinition definition = GuiDsl.chest("menu.track", 6).title("Track").build();
        assertTrue(port.open(new GuiRenderModel(sessionId, player.getUniqueId(), definition, GuiState.empty())));
        assertTrue(port.render(sessionId, new GuiRenderPatch(sessionId, GuiState.withData(1L, java.util.Map.of()))));
        assertTrue(port.close(sessionId, GuiCloseReason.USER_CLOSE));
        assertTrue(port.close(sessionId, GuiCloseReason.USER_CLOSE));

        assertEquals(1, backend.openCalls);
        assertEquals(1, backend.renderCalls);
        assertEquals(1, backend.closeCalls);
        assertFalse(port.render(sessionId, new GuiRenderPatch(sessionId, GuiState.empty())));
    }

    private static final class TrackingBackend implements InvUiGuiPort.Backend {
        private final InvUiGuiPort.BackendView view = new InvUiGuiPort.BackendView() {
        };
        private int openCalls;
        private int renderCalls;
        private int closeCalls;

        @Override
        public InvUiGuiPort.BackendView open(Player player, GuiRenderModel renderModel) {
            openCalls++;
            return view;
        }

        @Override
        public boolean render(InvUiGuiPort.BackendView view, GuiRenderPatch patch) {
            renderCalls++;
            return true;
        }

        @Override
        public boolean close(InvUiGuiPort.BackendView view, GuiCloseReason reason) {
            closeCalls++;
            return true;
        }
    }
}
