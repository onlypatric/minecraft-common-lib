package dev.patric.commonlib.team;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.match.DisconnectResult;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.api.match.MatchTimingPolicy;
import dev.patric.commonlib.api.match.RejoinPolicy;
import dev.patric.commonlib.api.match.RejoinResult;
import dev.patric.commonlib.api.team.FriendlyFirePolicy;
import dev.patric.commonlib.api.team.TeamDefinition;
import dev.patric.commonlib.api.team.TeamService;
import dev.patric.commonlib.runtime.MatchFoundationHooks;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TeamRejoinConsistencyTest {

    private ServerMock server;
    private TestPlugin plugin;
    private MatchEngineService matchEngine;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        matchEngine = runtime.services().require(MatchEngineService.class);
        teamService = runtime.services().require(TeamService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void disconnectAndRejoinKeepTeamAssignmentAndCleanupCanBeHookedOnEnd() {
        UUID player = UUID.randomUUID();

        MatchPolicy policy = new MatchPolicy(new MatchTimingPolicy(5L, 0L, 1L), RejoinPolicy.competitiveDefaults());
        MatchCallbacks callbacks = MatchFoundationHooks.withTeamCleanup(new MatchCallbacks() {
        }, teamService);

        var session = matchEngine.open(new MatchOpenRequest(
                "match.team.rejoin",
                policy,
                callbacks,
                (match, reason, services) -> {
                },
                Set.of(player)
        ));

        teamService.createRoster(
                session.matchId(),
                List.of(
                        new TeamDefinition("red", "Red", 5),
                        new TeamDefinition("blue", "Blue", 5)
                ),
                FriendlyFirePolicy.DENY_SAME_TEAM
        );
        assertEquals(dev.patric.commonlib.api.team.TeamAssignmentResult.ASSIGNED,
                teamService.assign(session.matchId(), player, "red"));

        assertEquals(DisconnectResult.MARKED_DISCONNECTED, matchEngine.disconnect(session.matchId(), player));
        server.getScheduler().performTicks(50L);

        assertEquals(RejoinResult.REJOINED, matchEngine.rejoin(session.matchId(), player));
        assertEquals("red", teamService.teamOf(session.matchId(), player).orElseThrow());

        matchEngine.end(session.matchId(), EndReason.ADMIN_STOP);
        server.getScheduler().performTicks(2L);

        assertFalse(matchEngine.find(session.matchId()).isPresent());
        assertFalse(teamService.snapshot(session.matchId()).isPresent());
    }
}
