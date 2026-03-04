package dev.patric.commonlib.match;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.match.DisconnectResult;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.api.match.MatchState;
import dev.patric.commonlib.api.match.MatchTimingPolicy;
import dev.patric.commonlib.api.match.RejoinPolicy;
import dev.patric.commonlib.api.match.RejoinResult;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchRejoinSessionPolicyTest {

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
    void rejoinWindowAndSessionTimeoutAreEnforced() {
        UUID playerId = UUID.randomUUID();

        MatchSession opened = matchEngine.open(new MatchOpenRequest(
                "duel.rejoin",
                new MatchPolicy(
                        new MatchTimingPolicy(50L, 0L, 10L),
                        new RejoinPolicy(true, 5L, 8L)
                ),
                new MatchCallbacks() {
                },
                (session, reason, services) -> {
                },
                Set.of(playerId)
        ));

        assertEquals(DisconnectResult.MARKED_DISCONNECTED, matchEngine.disconnect(opened.matchId(), playerId));

        server.getScheduler().performTicks(4L);
        assertEquals(RejoinResult.REJOINED, matchEngine.rejoin(opened.matchId(), playerId));

        assertEquals(DisconnectResult.MARKED_DISCONNECTED, matchEngine.disconnect(opened.matchId(), playerId));

        server.getScheduler().performTicks(6L);
        assertEquals(RejoinResult.WINDOW_EXPIRED, matchEngine.rejoin(opened.matchId(), playerId));

        server.getScheduler().performTicks(3L);
        assertEquals(RejoinResult.NOT_PARTICIPANT, matchEngine.rejoin(opened.matchId(), playerId));
        assertEquals(RejoinResult.NOT_PARTICIPANT, matchEngine.rejoin(opened.matchId(), playerId));
    }

    @Test
    void rejoinDeniedWhenStateIsNotEligible() {
        UUID playerId = UUID.randomUUID();

        MatchSession opened = matchEngine.open(new MatchOpenRequest(
                "duel.rejoin.state",
                new MatchPolicy(new MatchTimingPolicy(1L, 0L, 10L), RejoinPolicy.competitiveDefaults()),
                new MatchCallbacks() {
                },
                (session, reason, services) -> {
                },
                Set.of(playerId)
        ));

        matchEngine.startCountdown(opened.matchId());
        server.getScheduler().performTicks(1L);
        assertEquals(MatchState.RUNNING, matchEngine.find(opened.matchId()).orElseThrow().state());

        matchEngine.end(opened.matchId(), EndReason.ADMIN_STOP);
        assertEquals(MatchState.ENDING, matchEngine.find(opened.matchId()).orElseThrow().state());

        assertEquals(DisconnectResult.MARKED_DISCONNECTED, matchEngine.disconnect(opened.matchId(), playerId));
        assertEquals(RejoinResult.DENIED_STATE, matchEngine.rejoin(opened.matchId(), playerId));
    }
}
