package dev.patric.commonlib.hud;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.hud.BossBarOpenRequest;
import dev.patric.commonlib.api.hud.BossBarService;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionService;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudCleanupPolicyTest {

    private ServerMock server;
    private TestPlugin plugin;
    private CommonRuntime runtime;
    private ScoreboardSessionService scoreboards;
    private BossBarService bossBars;
    private GuiSessionService gui;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        scoreboards = runtime.services().require(ScoreboardSessionService.class);
        bossBars = runtime.services().require(BossBarService.class);
        gui = runtime.services().require(GuiSessionService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void quitAndWorldChangeCleanupCloseAudienceResources() {
        UUID playerOne = UUID.randomUUID();
        UUID playerTwo = UUID.randomUUID();

        ScoreboardSession s1 = scoreboards.open(new ScoreboardOpenRequest(playerOne, "sb.1", new ScoreboardSnapshot("T", List.of("a"))));
        ScoreboardSession s2 = scoreboards.open(new ScoreboardOpenRequest(playerTwo, "sb.2", new ScoreboardSnapshot("T", List.of("b"))));
        BossBarSession b1 = bossBars.open(new BossBarOpenRequest(playerOne, "bar.1", new BossBarState("HP", 1.0f, HudBarColor.RED, HudBarStyle.SOLID, true)));
        BossBarSession b2 = bossBars.open(new BossBarOpenRequest(playerTwo, "bar.2", new BossBarState("HP", 1.0f, HudBarColor.BLUE, HudBarStyle.SOLID, true)));

        scoreboards.onPlayerQuit(playerOne);
        bossBars.onPlayerQuit(playerOne);

        assertFalse(scoreboards.find(s1.sessionId()).isPresent());
        assertFalse(bossBars.find(b1.barId()).isPresent());
        assertTrue(scoreboards.find(s2.sessionId()).isPresent());
        assertTrue(bossBars.find(b2.barId()).isPresent());

        scoreboards.onPlayerWorldChange(playerTwo);
        bossBars.onPlayerWorldChange(playerTwo);

        assertFalse(scoreboards.find(s2.sessionId()).isPresent());
        assertFalse(bossBars.find(b2.barId()).isPresent());
    }

    @Test
    void runtimeDisableClosesHudAndGuiResources() {
        runtime.onLoad();
        runtime.onEnable();

        ScoreboardSession scoreboard = scoreboards.open(new ScoreboardOpenRequest(
                UUID.randomUUID(),
                "sb.disable",
                new ScoreboardSnapshot("T", List.of("line"))
        ));
        BossBarSession bossBar = bossBars.open(new BossBarOpenRequest(
                UUID.randomUUID(),
                "bar.disable",
                new BossBarState("HP", 0.9f, HudBarColor.GREEN, HudBarStyle.SOLID, true)
        ));
        GuiSession guiSession = gui.open(new GuiOpenRequest(UUID.randomUUID(), "gui.disable", null, 0L));

        runtime.onDisable();

        assertFalse(scoreboards.find(scoreboard.sessionId()).isPresent());
        assertFalse(bossBars.find(bossBar.barId()).isPresent());
        assertFalse(gui.find(guiSession.sessionId()).isPresent());
    }
}
