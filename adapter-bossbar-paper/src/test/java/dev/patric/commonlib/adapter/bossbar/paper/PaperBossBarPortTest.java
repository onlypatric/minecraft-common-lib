package dev.patric.commonlib.adapter.bossbar.paper;

import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperBossBarPortTest {

    private ServerMock server;
    private TestPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void openRenderCloseFlowIsDeterministicAndSafe() {
        PaperBossBarPort port = new PaperBossBarPort();

        BossBarSession session = new BossBarSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main",
                new BossBarState("Boss", 1.0f, HudBarColor.BLUE, HudBarStyle.SEGMENTED_10, true),
                false,
                System.currentTimeMillis(),
                0L
        );

        // no online player with this UUID -> open fails gracefully.
        assertFalse(port.open(session));
        assertFalse(port.render(session.barId(), new BossBarState("B", 0.5f, HudBarColor.RED, HudBarStyle.SOLID, true)));
        assertTrue(port.close(session.barId(), HudAudienceCloseReason.MANUAL));
        assertTrue(port.close(session.barId(), HudAudienceCloseReason.MANUAL));
    }

    public static class TestPlugin extends JavaPlugin {
        // no-op
    }
}
