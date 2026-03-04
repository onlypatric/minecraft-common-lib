package dev.patric.commonlib.hud;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionService;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.hud.ScoreboardUpdateResult;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardSessionLifecycleTest {

    private ServerMock server;
    private TestPlugin plugin;
    private ScoreboardSessionService service;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        service = runtime.services().require(ScoreboardSessionService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void openFindUpdateAndCloseLifecycleWorks() {
        UUID playerId = UUID.randomUUID();
        ScoreboardSession opened = service.open(new ScoreboardOpenRequest(
                playerId,
                "arena.main",
                new ScoreboardSnapshot("Arena", List.of("Line 1"))
        ));

        assertTrue(service.find(opened.sessionId()).isPresent());
        assertEquals(1, service.activeByPlayer(playerId).size());

        server.getScheduler().performTicks(5L);
        ScoreboardUpdateResult updateResult = service.update(
                opened.sessionId(),
                new ScoreboardSnapshot("Arena", List.of("Line 2"))
        );
        assertEquals(ScoreboardUpdateResult.APPLIED, updateResult);

        assertTrue(service.close(opened.sessionId(), HudAudienceCloseReason.MANUAL));
        assertFalse(service.close(opened.sessionId(), HudAudienceCloseReason.MANUAL));
        assertFalse(service.find(opened.sessionId()).isPresent());
    }

    @Test
    void invalidPayloadAndMissingSessionAreHandled() {
        ScoreboardUpdateResult missing = service.update(UUID.randomUUID(), new ScoreboardSnapshot("t", List.of("l")));
        assertEquals(ScoreboardUpdateResult.SESSION_NOT_FOUND, missing);

        List<String> tooManyLines = java.util.stream.IntStream.range(0, 16).mapToObj(i -> "l" + i).toList();
        ScoreboardSession opened = service.open(new ScoreboardOpenRequest(
                UUID.randomUUID(),
                "arena.payload",
                new ScoreboardSnapshot("Arena", List.of("l0"))
        ));

        ScoreboardUpdateResult invalid = service.update(opened.sessionId(), new ScoreboardSnapshot("Arena", tooManyLines));
        assertEquals(ScoreboardUpdateResult.INVALID_PAYLOAD, invalid);
    }
}
