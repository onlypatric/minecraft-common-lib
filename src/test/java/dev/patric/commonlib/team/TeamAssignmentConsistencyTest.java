package dev.patric.commonlib.team;

import dev.patric.commonlib.api.team.FriendlyFirePolicy;
import dev.patric.commonlib.api.team.TeamAssignmentResult;
import dev.patric.commonlib.api.team.TeamDefinition;
import dev.patric.commonlib.runtime.DefaultTeamService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamAssignmentConsistencyTest {

    @Test
    void manualAndAutoAssignmentRespectCapacityAndPolicy() {
        DefaultTeamService service = new DefaultTeamService();
        UUID matchId = UUID.randomUUID();

        service.createRoster(
                matchId,
                List.of(
                        new TeamDefinition("red", "Red", 2),
                        new TeamDefinition("blue", "Blue", 2)
                ),
                FriendlyFirePolicy.DENY_SAME_TEAM
        );

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();
        UUID p4 = UUID.randomUUID();
        UUID p5 = UUID.randomUUID();

        assertEquals(TeamAssignmentResult.ASSIGNED, service.assign(matchId, p1, "red"));
        assertEquals(TeamAssignmentResult.ALREADY_ASSIGNED, service.assign(matchId, p1, "red"));
        assertEquals(TeamAssignmentResult.ASSIGNED, service.assign(matchId, p2, "blue"));
        assertEquals(TeamAssignmentResult.ASSIGNED, service.assign(matchId, p3, "red"));
        assertEquals(TeamAssignmentResult.TEAM_FULL, service.assign(matchId, p4, "red"));
        assertEquals(TeamAssignmentResult.ASSIGNED, service.autoAssign(matchId, p4));
        assertEquals(TeamAssignmentResult.TEAM_FULL, service.autoAssign(matchId, p5));

        assertEquals("red", service.teamOf(matchId, p1).orElseThrow());
        assertEquals("blue", service.teamOf(matchId, p2).orElseThrow());

        assertFalse(service.canDamage(matchId, p1, p3));
        assertTrue(service.canDamage(matchId, p1, p2));

        service.removePlayer(matchId, p3);
        assertTrue(service.canDamage(matchId, p1, p3));

        var snapshot = service.snapshot(matchId).orElseThrow();
        assertEquals(1, snapshot.teams().get("red").size());
        assertEquals(2, snapshot.teams().get("blue").size());
    }
}
