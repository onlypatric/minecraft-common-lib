package dev.patric.commonlib.team;

import dev.patric.commonlib.api.team.PartyActionResult;
import dev.patric.commonlib.runtime.DefaultPartyService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartyServiceFlowTest {

    @Test
    void leaderInviteAcceptKickLeaveAndDisbandFlowIsConsistent() {
        DefaultPartyService service = new DefaultPartyService();
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        var party = service.create(leader);

        assertEquals(PartyActionResult.NOT_LEADER, service.invite(party.partyId(), other, member));
        assertEquals(PartyActionResult.APPLIED, service.invite(party.partyId(), leader, member));
        assertEquals(PartyActionResult.APPLIED, service.acceptInvite(party.partyId(), member));
        assertEquals(PartyActionResult.ALREADY_MEMBER, service.acceptInvite(party.partyId(), member));

        assertEquals(PartyActionResult.NOT_LEADER, service.kick(party.partyId(), member, leader));
        assertEquals(PartyActionResult.APPLIED, service.kick(party.partyId(), leader, member));
        assertFalse(service.findByMember(member).isPresent());

        assertEquals(PartyActionResult.APPLIED, service.invite(party.partyId(), leader, other));
        assertEquals(PartyActionResult.APPLIED, service.acceptInvite(party.partyId(), other));
        assertEquals(PartyActionResult.APPLIED, service.leave(party.partyId(), other));

        assertTrue(service.find(party.partyId()).isPresent());
        assertEquals(PartyActionResult.APPLIED, service.disband(party.partyId(), leader));
        assertFalse(service.find(party.partyId()).isPresent());
        assertFalse(service.findByMember(leader).isPresent());
    }
}
