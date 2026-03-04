package dev.patric.commonlib.api.team;

import java.util.Optional;
import java.util.UUID;

/**
 * Party service for player group lifecycle.
 */
public interface PartyService {

    /**
     * Creates a party with the given leader.
     *
     * @param leaderId leader id.
     * @return created party snapshot.
     */
    PartySnapshot create(UUID leaderId);

    /**
     * Sends invite from leader to target player.
     *
     * @param partyId party id.
     * @param leaderId leader id.
     * @param targetId target player id.
     * @return party action result.
     */
    PartyActionResult invite(UUID partyId, UUID leaderId, UUID targetId);

    /**
     * Accepts pending invite.
     *
     * @param partyId party id.
     * @param targetId target player id.
     * @return party action result.
     */
    PartyActionResult acceptInvite(UUID partyId, UUID targetId);

    /**
     * Kicks member from party.
     *
     * @param partyId party id.
     * @param leaderId leader id.
     * @param memberId member id.
     * @return party action result.
     */
    PartyActionResult kick(UUID partyId, UUID leaderId, UUID memberId);

    /**
     * Removes member from current party.
     *
     * @param partyId party id.
     * @param memberId member id.
     * @return party action result.
     */
    PartyActionResult leave(UUID partyId, UUID memberId);

    /**
     * Disbands party.
     *
     * @param partyId party id.
     * @param leaderId leader id.
     * @return party action result.
     */
    PartyActionResult disband(UUID partyId, UUID leaderId);

    /**
     * Finds party by id.
     *
     * @param partyId party id.
     * @return party snapshot when present.
     */
    Optional<PartySnapshot> find(UUID partyId);

    /**
     * Finds party by member id.
     *
     * @param playerId player id.
     * @return party snapshot when player is a member.
     */
    Optional<PartySnapshot> findByMember(UUID playerId);
}
