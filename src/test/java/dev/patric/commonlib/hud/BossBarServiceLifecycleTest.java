package dev.patric.commonlib.hud;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.hud.BossBarOpenRequest;
import dev.patric.commonlib.api.hud.BossBarService;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.BossBarUpdateResult;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossBarServiceLifecycleTest {

    private ServerMock server;
    private TestPlugin plugin;
    private BossBarService service;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        service = runtime.services().require(BossBarService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void openFindUpdateAndCloseLifecycleWorks() {
        UUID playerId = UUID.randomUUID();
        BossBarSession opened = service.open(new BossBarOpenRequest(
                playerId,
                "bar.main",
                new BossBarState("Fight", 1.0f, HudBarColor.RED, HudBarStyle.SOLID, true)
        ));

        assertTrue(service.find(opened.barId()).isPresent());
        assertEquals(1, service.activeByPlayer(playerId).size());

        server.getScheduler().performTicks(5L);
        BossBarUpdateResult update = service.update(
                opened.barId(),
                new BossBarState("Fight", 0.5f, HudBarColor.YELLOW, HudBarStyle.SEGMENTED_10, true)
        );
        assertEquals(BossBarUpdateResult.APPLIED, update);

        assertTrue(service.close(opened.barId(), HudAudienceCloseReason.MANUAL));
        assertFalse(service.close(opened.barId(), HudAudienceCloseReason.MANUAL));
        assertFalse(service.find(opened.barId()).isPresent());
    }

    @Test
    void invalidStateAndMissingBarAreHandled() {
        BossBarUpdateResult missing = service.update(
                UUID.randomUUID(),
                new BossBarState("missing", 1.0f, HudBarColor.BLUE, HudBarStyle.SOLID, true)
        );
        assertEquals(BossBarUpdateResult.BAR_NOT_FOUND, missing);

        BossBarSession opened = service.open(new BossBarOpenRequest(
                UUID.randomUUID(),
                "bar.invalid",
                new BossBarState("Start", 1.0f, HudBarColor.GREEN, HudBarStyle.SOLID, true)
        ));

        BossBarUpdateResult invalid = service.update(
                opened.barId(),
                new BossBarState(" ", 0.7f, HudBarColor.GREEN, HudBarStyle.SOLID, true)
        );
        assertEquals(BossBarUpdateResult.INVALID_STATE, invalid);
    }
}
