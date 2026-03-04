package dev.patric.commonlib.adapter.invui;

import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiItemView;
import dev.patric.commonlib.api.gui.GuiPortFeature;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.SlotInteractionPolicy;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvUiGuiPortRenderTest {

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
    void openRenderCloseAreDeterministicAndSafe() {
        PlayerMock player = server.addPlayer();
        UUID sessionId = UUID.randomUUID();

        GuiDefinition definition = GuiDsl.chest("menu.invui", 6)
                .title("Menu")
                .slot(13, slot -> slot
                        .item(GuiItemView.of("DIAMOND", "Buy"))
                        .interaction(SlotInteractionPolicy.BUTTON_ONLY))
                .build();

        FakeBackend backend = new FakeBackend();
        InvUiGuiPort port = new InvUiGuiPort(backend);

        assertTrue(port.open(new GuiRenderModel(sessionId, player.getUniqueId(), definition, GuiState.empty())));
        assertTrue(port.render(sessionId, new GuiRenderPatch(sessionId, GuiState.withData(1L, Map.of("page", "2")))));
        assertTrue(port.close(sessionId, dev.patric.commonlib.api.gui.GuiCloseReason.USER_CLOSE));
        assertTrue(port.close(sessionId, dev.patric.commonlib.api.gui.GuiCloseReason.USER_CLOSE));

        assertTrue(port.supports(GuiPortFeature.CLICK));
        assertTrue(port.supports(GuiPortFeature.DRAG));
        assertTrue(port.supports(GuiPortFeature.SHIFT_TRANSFER));
        assertTrue(port.supports(GuiPortFeature.HOTBAR_SWAP));
        assertTrue(port.supports(GuiPortFeature.DOUBLE_CLICK));
        assertFalse(port.supports(GuiPortFeature.DIALOG_BRIDGE));

        assertTrue(backend.openCalled);
        assertTrue(backend.renderCalled);
        assertTrue(backend.closeCalled);
    }

    private static final class FakeBackend implements InvUiGuiPort.Backend {
        private final InvUiGuiPort.BackendView view = new FakeView();
        private boolean openCalled;
        private boolean renderCalled;
        private boolean closeCalled;

        @Override
        public InvUiGuiPort.BackendView open(org.bukkit.entity.Player player, GuiRenderModel renderModel) {
            openCalled = true;
            return view;
        }

        @Override
        public boolean render(InvUiGuiPort.BackendView view, GuiRenderPatch patch) {
            renderCalled = true;
            return true;
        }

        @Override
        public boolean close(InvUiGuiPort.BackendView view, dev.patric.commonlib.api.gui.GuiCloseReason reason) {
            closeCalled = true;
            return true;
        }
    }

    private record FakeView() implements InvUiGuiPort.BackendView {
    }
}
