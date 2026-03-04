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
import dev.patric.commonlib.api.match.MatchTransitionResult;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchStateTransitionTest {

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
    void validAndInvalidTransitionsAreHandledDeterministically() {
        MatchSession session = matchEngine.open(new MatchOpenRequest(
                "duel.transition",
                new MatchPolicy(new MatchTimingPolicy(3L, 0L, 1L), RejoinPolicy.competitiveDefaults()),
                new MatchCallbacks() {
                },
                (match, reason, services) -> {
                },
                Set.of(UUID.randomUUID())
        ));

        assertEquals(MatchState.LOBBY, session.state());
        assertEquals(MatchTransitionResult.INVALID_TRANSITION,
                matchEngine.transition(session.matchId(), MatchState.RUNNING, EndReason.ADMIN_STOP));

        assertEquals(MatchTransitionResult.APPLIED, matchEngine.startCountdown(session.matchId()));
        assertEquals(MatchState.COUNTDOWN, matchEngine.find(session.matchId()).orElseThrow().state());

        assertEquals(MatchTransitionResult.INVALID_TRANSITION,
                matchEngine.transition(session.matchId(), MatchState.RESET, EndReason.ADMIN_STOP));

        assertEquals(MatchTransitionResult.APPLIED,
                matchEngine.transition(session.matchId(), MatchState.RUNNING, EndReason.ADMIN_STOP));
        assertEquals(MatchState.RUNNING, matchEngine.find(session.matchId()).orElseThrow().state());

        assertEquals(MatchTransitionResult.APPLIED,
                matchEngine.transition(session.matchId(), MatchState.ENDING, EndReason.ADMIN_STOP));
        assertEquals(MatchState.ENDING, matchEngine.find(session.matchId()).orElseThrow().state());

        assertEquals(MatchTransitionResult.APPLIED,
                matchEngine.transition(session.matchId(), MatchState.RESET, EndReason.ADMIN_STOP));
        assertEquals(MatchState.RESET, matchEngine.find(session.matchId()).orElseThrow().state());

        server.getScheduler().performTicks(1L);
        assertFalse(matchEngine.find(session.matchId()).isPresent());
        assertTrue(matchEngine.isIdle());
    }

    @Test
    void timerDrivenTransitionsFollowConfiguredPolicy() {
        MatchSession session = matchEngine.open(new MatchOpenRequest(
                "duel.timers",
                new MatchPolicy(new MatchTimingPolicy(2L, 3L, 1L), RejoinPolicy.competitiveDefaults()),
                new MatchCallbacks() {
                },
                (match, reason, services) -> {
                },
                Set.of(UUID.randomUUID())
        ));

        assertEquals(MatchTransitionResult.APPLIED, matchEngine.startCountdown(session.matchId()));

        server.getScheduler().performTicks(2L);
        assertEquals(MatchState.RUNNING, matchEngine.find(session.matchId()).orElseThrow().state());

        server.getScheduler().performTicks(3L);
        MatchSession ending = matchEngine.find(session.matchId()).orElseThrow();
        assertEquals(MatchState.ENDING, ending.state());
        assertEquals(EndReason.TIME_LIMIT, ending.lastEndReason());

        server.getScheduler().performTicks(1L);
        assertEquals(MatchState.RESET, matchEngine.find(session.matchId()).orElseThrow().state());

        server.getScheduler().performTicks(1L);
        assertFalse(matchEngine.find(session.matchId()).isPresent());
        assertTrue(matchEngine.isIdle());
    }
}
