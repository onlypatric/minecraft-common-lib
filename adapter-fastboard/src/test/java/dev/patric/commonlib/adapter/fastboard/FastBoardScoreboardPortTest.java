package dev.patric.commonlib.adapter.fastboard;

import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import java.util.List;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FastBoardScoreboardPortTest {

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
    void openRenderCloseFlowIsSafeAndDeterministic() {
        PlayerMock player = server.addPlayer();
        FastBoardScoreboardPort port = new FastBoardScoreboardPort();

        ScoreboardSession session = new ScoreboardSession(
                UUID.randomUUID(),
                player.getUniqueId(),
                "main",
                new ScoreboardSnapshot("Lobby", List.of("Players: 1")),
                ScoreboardSessionStatus.OPEN,
                System.currentTimeMillis(),
                0L
        );

        assertFalse(port.open(session));
        assertFalse(port.render(session.sessionId(), new ScoreboardSnapshot("Lobby", List.of("Players: 2"))));
        assertTrue(port.close(session.sessionId(), HudAudienceCloseReason.MANUAL));
        assertTrue(port.close(session.sessionId(), HudAudienceCloseReason.MANUAL));
    }

    @Test
    void openFailsGracefullyWhenPlayerIsMissing() {
        FastBoardScoreboardPort port = new FastBoardScoreboardPort();

        ScoreboardSession session = new ScoreboardSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "missing",
                new ScoreboardSnapshot("Missing", List.of("none")),
                ScoreboardSessionStatus.OPEN,
                System.currentTimeMillis(),
                0L
        );

        assertFalse(port.open(session));
    }

    public static class TestPlugin extends JavaPlugin {
        // no-op
    }
}
