package dev.patric.commonlib.match;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.api.match.MatchState;
import dev.patric.commonlib.api.match.MatchTimingPolicy;
import dev.patric.commonlib.api.match.RejoinPolicy;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiMatchIsolationTest {

    private ServerMock server;
    private TestPlugin plugin;
    private MatchEngineService matchEngine;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        matchEngine = runtime.services().require(MatchEngineService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void matchesRemainIsolatedAcrossTransitionsAndClosePaths() {
        AtomicInteger firstCleanup = new AtomicInteger();
        AtomicInteger secondCleanup = new AtomicInteger();

        MatchSession first = matchEngine.open(new MatchOpenRequest(
                "arena.first",
                new MatchPolicy(new MatchTimingPolicy(1L, 0L, 1L), RejoinPolicy.competitiveDefaults()),
                new MatchCallbacks() {
                },
                (session, reason, services) -> firstCleanup.incrementAndGet(),
                Set.of(UUID.randomUUID())
        ));
        MatchSession second = matchEngine.open(new MatchOpenRequest(
                "arena.second",
                new MatchPolicy(new MatchTimingPolicy(4L, 0L, 1L), RejoinPolicy.competitiveDefaults()),
                new MatchCallbacks() {
                },
                (session, reason, services) -> secondCleanup.incrementAndGet(),
                Set.of(UUID.randomUUID())
        ));

        matchEngine.startCountdown(first.matchId());
        matchEngine.startCountdown(second.matchId());

        server.getScheduler().performTicks(1L);
        assertEquals(MatchState.RUNNING, matchEngine.find(first.matchId()).orElseThrow().state());
        assertEquals(MatchState.COUNTDOWN, matchEngine.find(second.matchId()).orElseThrow().state());

        server.getScheduler().performTicks(2L);
        assertEquals(MatchState.COUNTDOWN, matchEngine.find(second.matchId()).orElseThrow().state());

        matchEngine.end(first.matchId(), EndReason.ADMIN_STOP);
        server.getScheduler().performTicks(3L);

        assertFalse(matchEngine.find(first.matchId()).isPresent());
        MatchSession secondSnapshot = matchEngine.find(second.matchId()).orElseThrow();
        assertEquals(MatchState.RUNNING, secondSnapshot.state());

        assertEquals(1, firstCleanup.get());
        assertEquals(0, secondCleanup.get());

        assertEquals(1, matchEngine.closeAll(EndReason.ADMIN_STOP));
        assertTrue(matchEngine.active().isEmpty());
    }
}
