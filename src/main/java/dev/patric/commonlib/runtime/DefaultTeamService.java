package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.team.FriendlyFirePolicy;
import dev.patric.commonlib.api.team.TeamAssignmentResult;
import dev.patric.commonlib.api.team.TeamDefinition;
import dev.patric.commonlib.api.team.TeamService;
import dev.patric.commonlib.api.team.TeamSnapshot;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory team service.
 */
public final class DefaultTeamService implements TeamService {

    private final Map<UUID, RosterRecord> rosters = new ConcurrentHashMap<>();

    @Override
    public void createRoster(UUID matchId, List<TeamDefinition> definitions, FriendlyFirePolicy policy) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(definitions, "definitions");
        Objects.requireNonNull(policy, "policy");

        if (definitions.isEmpty()) {
            throw new IllegalArgumentException("definitions must not be empty");
        }

        Map<String, TeamBucket> buckets = new LinkedHashMap<>();
        for (TeamDefinition definition : definitions) {
            TeamBucket previous = buckets.putIfAbsent(definition.teamId(), new TeamBucket(definition));
            if (previous != null) {
                throw new IllegalArgumentException("duplicate team id: " + definition.teamId());
            }
        }

        rosters.put(matchId, new RosterRecord(matchId, buckets, policy));
    }

    @Override
    public TeamAssignmentResult assign(UUID matchId, UUID playerId, String teamId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");
        String normalizedTeamId = normalizeText(teamId, "teamId");

        RosterRecord roster = rosters.get(matchId);
        if (roster == null) {
            return TeamAssignmentResult.MATCH_NOT_FOUND;
        }

        synchronized (roster) {
            TeamBucket target = roster.buckets.get(normalizedTeamId);
            if (target == null) {
                return TeamAssignmentResult.TEAM_NOT_FOUND;
            }

            String currentTeamId = roster.assignments.get(playerId);
            if (normalizedTeamId.equals(currentTeamId)) {
                return TeamAssignmentResult.ALREADY_ASSIGNED;
            }

            if (target.members.size() >= target.definition.maxMembers()) {
                return TeamAssignmentResult.TEAM_FULL;
            }

            if (currentTeamId != null) {
                TeamBucket current = roster.buckets.get(currentTeamId);
                if (current != null) {
                    current.members.remove(playerId);
                }
            }

            target.members.add(playerId);
            roster.assignments.put(playerId, normalizedTeamId);
            return TeamAssignmentResult.ASSIGNED;
        }
    }

    @Override
    public TeamAssignmentResult autoAssign(UUID matchId, UUID playerId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");

        RosterRecord roster = rosters.get(matchId);
        if (roster == null) {
            return TeamAssignmentResult.MATCH_NOT_FOUND;
        }

        synchronized (roster) {
            String current = roster.assignments.get(playerId);
            if (current != null) {
                return TeamAssignmentResult.ALREADY_ASSIGNED;
            }

            TeamBucket target = roster.buckets.values().stream()
                    .filter(bucket -> bucket.members.size() < bucket.definition.maxMembers())
                    .min(Comparator.comparingInt(bucket -> bucket.members.size()))
                    .orElse(null);

            if (target == null) {
                return TeamAssignmentResult.TEAM_FULL;
            }

            target.members.add(playerId);
            roster.assignments.put(playerId, target.definition.teamId());
            return TeamAssignmentResult.ASSIGNED;
        }
    }

    @Override
    public Optional<String> teamOf(UUID matchId, UUID playerId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");

        RosterRecord roster = rosters.get(matchId);
        if (roster == null) {
            return Optional.empty();
        }

        synchronized (roster) {
            return Optional.ofNullable(roster.assignments.get(playerId));
        }
    }

    @Override
    public boolean canDamage(UUID matchId, UUID attackerId, UUID victimId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(attackerId, "attackerId");
        Objects.requireNonNull(victimId, "victimId");

        RosterRecord roster = rosters.get(matchId);
        if (roster == null) {
            return true;
        }

        synchronized (roster) {
            if (roster.policy == FriendlyFirePolicy.ALLOW) {
                return true;
            }

            String attackerTeam = roster.assignments.get(attackerId);
            String victimTeam = roster.assignments.get(victimId);
            if (attackerTeam == null || victimTeam == null) {
                return true;
            }
            return !attackerTeam.equals(victimTeam);
        }
    }

    @Override
    public void removePlayer(UUID matchId, UUID playerId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");

        RosterRecord roster = rosters.get(matchId);
        if (roster == null) {
            return;
        }

        synchronized (roster) {
            String teamId = roster.assignments.remove(playerId);
            if (teamId == null) {
                return;
            }
            TeamBucket bucket = roster.buckets.get(teamId);
            if (bucket != null) {
                bucket.members.remove(playerId);
            }
        }
    }

    @Override
    public Optional<TeamSnapshot> snapshot(UUID matchId) {
        Objects.requireNonNull(matchId, "matchId");

        RosterRecord roster = rosters.get(matchId);
        if (roster == null) {
            return Optional.empty();
        }

        synchronized (roster) {
            Map<String, Set<UUID>> teams = new HashMap<>();
            for (Map.Entry<String, TeamBucket> entry : roster.buckets.entrySet()) {
                teams.put(entry.getKey(), Set.copyOf(entry.getValue().members));
            }
            return Optional.of(new TeamSnapshot(matchId, teams, roster.policy));
        }
    }

    @Override
    public boolean clearRoster(UUID matchId) {
        Objects.requireNonNull(matchId, "matchId");
        return rosters.remove(matchId) != null;
    }

    private static String normalizeText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static final class RosterRecord {

        private final UUID matchId;
        private final Map<String, TeamBucket> buckets;
        private final Map<UUID, String> assignments;
        private final FriendlyFirePolicy policy;

        private RosterRecord(UUID matchId, Map<String, TeamBucket> buckets, FriendlyFirePolicy policy) {
            this.matchId = matchId;
            this.buckets = buckets;
            this.assignments = new HashMap<>();
            this.policy = policy;
        }
    }

    private static final class TeamBucket {

        private final TeamDefinition definition;
        private final Set<UUID> members;

        private TeamBucket(TeamDefinition definition) {
            this.definition = definition;
            this.members = new HashSet<>();
        }
    }
}
