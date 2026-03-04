package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiOpenOptions;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiCloseTimeoutSubmitRaceTest {

    private ServerMock server;
    private TestPlugin plugin;
    private GuiSessionService gui;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        gui = runtime.services().require(GuiSessionService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void closeWinsAgainstScheduledTimeoutWithoutZombieSession() {
        Player player = server.addPlayer();
        GuiDefinition definition = GuiDsl.chest("menu.timeout", 6).build();
        GuiSession session = gui.open(definition, player.getUniqueId(), new GuiOpenOptions(1L, true, true, java.util.Locale.ENGLISH, java.util.Map.of()));

        assertTrue(gui.close(session.sessionId(), GuiCloseReason.USER_CLOSE));
        server.getScheduler().performTicks(5L);

        assertFalse(gui.find(session.sessionId()).isPresent());
    }
}
