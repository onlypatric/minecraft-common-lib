package dev.patric.commonlib.adapter.invui;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvUiFallbackNoDependencySmokeTest {

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
    void returnsFalseWhenBackendFailsWithoutThrowing() {
        PlayerMock player = server.addPlayer();
        InvUiGuiPort port = new InvUiGuiPort(new FailingBackend());

        boolean opened = port.open(new GuiRenderModel(
                UUID.randomUUID(),
                player.getUniqueId(),
                GuiDsl.chest("menu.fail", 6).title("Fail").build(),
                GuiState.empty()
        ));

        assertFalse(opened);
        assertTrue(port.close(UUID.randomUUID(), GuiCloseReason.ERROR));
    }

    private static final class FailingBackend implements InvUiGuiPort.Backend {
        @Override
        public InvUiGuiPort.BackendView open(Player player, GuiRenderModel renderModel) {
            throw new NoClassDefFoundError("xyz/xenondevs/invui/InvUI");
        }

        @Override
        public boolean render(InvUiGuiPort.BackendView view, dev.patric.commonlib.api.gui.render.GuiRenderPatch patch) {
            return false;
        }

        @Override
        public boolean close(InvUiGuiPort.BackendView view, GuiCloseReason reason) {
            return true;
        }
    }
}
