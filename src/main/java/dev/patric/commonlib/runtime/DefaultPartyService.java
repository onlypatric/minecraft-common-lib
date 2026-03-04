package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.team.PartyActionResult;
import dev.patric.commonlib.api.team.PartyService;
import dev.patric.commonlib.api.team.PartySnapshot;
import dev.patric.commonlib.api.team.PartyStatus;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Default in-memory party service.
 */
public final class DefaultPartyService implements PartyService {

    private final Map<UUID, PartyRecord> parties = new HashMap<>();
    private final Map<UUID, UUID> memberToParty = new HashMap<>();
    private final Object lock = new Object();

    @Override
    public PartySnapshot create(UUID leaderId) {
        Objects.requireNonNull(leaderId, "leaderId");

        synchronized (lock) {
            UUID existingPartyId = memberToParty.get(leaderId);
            if (existingPartyId != null) {
                PartyRecord existing = parties.get(existingPartyId);
                if (existing != null && existing.status == PartyStatus.ACTIVE) {
                    return existing.snapshot();
                }
            }

            UUID partyId = UUID.randomUUID();
            PartyRecord record = new PartyRecord(partyId, leaderId);
            parties.put(partyId, record);
            memberToParty.put(leaderId, partyId);
            return record.snapshot();
        }
    }

    @Override
    public PartyActionResult invite(UUID partyId, UUID leaderId, UUID targetId) {
        Objects.requireNonNull(partyId, "partyId");
        Objects.requireNonNull(leaderId, "leaderId");
        Objects.requireNonNull(targetId, "targetId");

        synchronized (lock) {
            PartyRecord record = parties.get(partyId);
            if (record == null || record.status != PartyStatus.ACTIVE) {
                return PartyActionResult.NOT_FOUND;
            }
            if (!record.leaderId.equals(leaderId)) {
                return PartyActionResult.NOT_LEADER;
            }
            if (record.members.contains(targetId)) {
                return PartyActionResult.ALREADY_MEMBER;
            }

            UUID existingPartyId = memberToParty.get(targetId);
            if (existingPartyId != null && !existingPartyId.equals(partyId)) {
                return PartyActionResult.DENIED;
            }

            record.pendingInvites.add(targetId);
            return PartyActionResult.APPLIED;
        }
    }

    @Override
    public PartyActionResult acceptInvite(UUID partyId, UUID targetId) {
        Objects.requireNonNull(partyId, "partyId");
        Objects.requireNonNull(targetId, "targetId");

        synchronized (lock) {
            PartyRecord record = parties.get(partyId);
            if (record == null || record.status != PartyStatus.ACTIVE) {
                return PartyActionResult.NOT_FOUND;
            }
            if (record.members.contains(targetId)) {
                return PartyActionResult.ALREADY_MEMBER;
            }
            if (!record.pendingInvites.contains(targetId)) {
                return PartyActionResult.INVITE_NOT_FOUND;
            }
            if (memberToParty.containsKey(targetId)) {
                return PartyActionResult.DENIED;
            }

            record.pendingInvites.remove(targetId);
            record.members.add(targetId);
            memberToParty.put(targetId, partyId);
            return PartyActionResult.APPLIED;
        }
    }

    @Override
    public PartyActionResult kick(UUID partyId, UUID leaderId, UUID memberId) {
        Objects.requireNonNull(partyId, "partyId");
        Objects.requireNonNull(leaderId, "leaderId");
        Objects.requireNonNull(memberId, "memberId");

        synchronized (lock) {
            PartyRecord record = parties.get(partyId);
            if (record == null || record.status != PartyStatus.ACTIVE) {
                return PartyActionResult.NOT_FOUND;
            }
            if (!record.leaderId.equals(leaderId)) {
                return PartyActionResult.NOT_LEADER;
            }
            if (record.leaderId.equals(memberId)) {
                return PartyActionResult.DENIED;
            }
            if (!record.members.remove(memberId)) {
                return PartyActionResult.NOT_FOUND;
            }

            record.pendingInvites.remove(memberId);
            memberToParty.remove(memberId);
            return PartyActionResult.APPLIED;
        }
    }

    @Override
    public PartyActionResult leave(UUID partyId, UUID memberId) {
        Objects.requireNonNull(partyId, "partyId");
        Objects.requireNonNull(memberId, "memberId");

        synchronized (lock) {
            PartyRecord record = parties.get(partyId);
            if (record == null || record.status != PartyStatus.ACTIVE) {
                return PartyActionResult.NOT_FOUND;
            }
            if (!record.members.contains(memberId)) {
                return PartyActionResult.NOT_FOUND;
            }

            if (record.leaderId.equals(memberId)) {
                disbandInternal(record);
                return PartyActionResult.APPLIED;
            }

            record.members.remove(memberId);
            record.pendingInvites.remove(memberId);
            memberToParty.remove(memberId);
            return PartyActionResult.APPLIED;
        }
    }

    @Override
    public PartyActionResult disband(UUID partyId, UUID leaderId) {
        Objects.requireNonNull(partyId, "partyId");
        Objects.requireNonNull(leaderId, "leaderId");

        synchronized (lock) {
            PartyRecord record = parties.get(partyId);
            if (record == null || record.status != PartyStatus.ACTIVE) {
                return PartyActionResult.NOT_FOUND;
            }
            if (!record.leaderId.equals(leaderId)) {
                return PartyActionResult.NOT_LEADER;
            }

            disbandInternal(record);
            return PartyActionResult.APPLIED;
        }
    }

    @Override
    public Optional<PartySnapshot> find(UUID partyId) {
        Objects.requireNonNull(partyId, "partyId");

        synchronized (lock) {
            PartyRecord record = parties.get(partyId);
            if (record == null || record.status != PartyStatus.ACTIVE) {
                return Optional.empty();
            }
            return Optional.of(record.snapshot());
        }
    }

    @Override
    public Optional<PartySnapshot> findByMember(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        synchronized (lock) {
            UUID partyId = memberToParty.get(playerId);
            if (partyId == null) {
                return Optional.empty();
            }
            return find(partyId);
        }
    }

    private void disbandInternal(PartyRecord record) {
        record.status = PartyStatus.DISBANDED;
        parties.remove(record.partyId);
        for (UUID member : Set.copyOf(record.members)) {
            memberToParty.remove(member);
        }
        record.members.clear();
        record.pendingInvites.clear();
    }

    private static final class PartyRecord {

        private final UUID partyId;
        private final UUID leaderId;
        private final Set<UUID> members;
        private final Set<UUID> pendingInvites;

        private PartyStatus status;

        private PartyRecord(UUID partyId, UUID leaderId) {
            this.partyId = partyId;
            this.leaderId = leaderId;
            this.members = new HashSet<>();
            this.pendingInvites = new HashSet<>();
            this.status = PartyStatus.ACTIVE;
            this.members.add(leaderId);
        }

        private PartySnapshot snapshot() {
            return new PartySnapshot(partyId, leaderId, Set.copyOf(members), Set.copyOf(pendingInvites), status);
        }
    }
}
