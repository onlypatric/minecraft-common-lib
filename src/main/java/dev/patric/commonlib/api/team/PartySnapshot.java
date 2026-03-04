package dev.patric.commonlib.api.team;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable party snapshot.
 *
 * @param partyId party id.
 * @param leaderId leader id.
 * @param members member set.
 * @param pendingInvites pending invites.
 * @param status party status.
 */
public record PartySnapshot(
        UUID partyId,
        UUID leaderId,
        Set<UUID> members,
        Set<UUID> pendingInvites,
        PartyStatus status
) {

    /**
     * Creates a party snapshot.
     */
    public PartySnapshot {
        partyId = Objects.requireNonNull(partyId, "partyId");
        leaderId = Objects.requireNonNull(leaderId, "leaderId");
        members = Set.copyOf(Objects.requireNonNull(members, "members"));
        pendingInvites = Set.copyOf(Objects.requireNonNull(pendingInvites, "pendingInvites"));
        status = Objects.requireNonNull(status, "status");
    }
}
