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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MatchRollbackSafetyTest {

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
    void callbackFailureForcesErrorEndAndCleanupOnce() {
        AtomicInteger cleanupCalls = new AtomicInteger();
        AtomicInteger onEndCalls = new AtomicInteger();
        AtomicReference<EndReason> lastReason = new AtomicReference<>();

        MatchCallbacks callbacks = new MatchCallbacks() {
            @Override
            public void onStateTick(MatchSession session, long stateTick) {
                if (session.state() == MatchState.RUNNING) {
                    throw new IllegalStateException("boom-running");
                }
            }

            @Override
            public void onEnd(MatchSession session, EndReason reason) {
                onEndCalls.incrementAndGet();
                lastReason.set(reason);
            }
        };

        MatchSession opened = matchEngine.open(new MatchOpenRequest(
                "duel.rollback.callback",
                new MatchPolicy(new MatchTimingPolicy(1L, 0L, 1L), RejoinPolicy.competitiveDefaults()),
                callbacks,
                (session, reason, services) -> cleanupCalls.incrementAndGet(),
                Set.of(UUID.randomUUID())
        ));

        matchEngine.startCountdown(opened.matchId());
        server.getScheduler().performTicks(2L);

        assertFalse(matchEngine.find(opened.matchId()).isPresent());
        assertEquals(1, cleanupCalls.get());
        assertEquals(1, onEndCalls.get());
        assertEquals(EndReason.ERROR, lastReason.get());
    }

    @Test
    void cleanupFailureDoesNotLeaveMatchZombie() {
        MatchSession opened = matchEngine.open(new MatchOpenRequest(
                "duel.rollback.cleanup",
                new MatchPolicy(new MatchTimingPolicy(1L, 0L, 1L), RejoinPolicy.competitiveDefaults()),
                new MatchCallbacks() {
                },
                (session, reason, services) -> {
                    throw new IllegalStateException("cleanup failure");
                },
                Set.of(UUID.randomUUID())
        ));

        matchEngine.end(opened.matchId(), EndReason.ADMIN_STOP);
        server.getScheduler().performTicks(3L);

        assertFalse(matchEngine.find(opened.matchId()).isPresent());
        assertEquals(0, matchEngine.active().size());
    }
}
