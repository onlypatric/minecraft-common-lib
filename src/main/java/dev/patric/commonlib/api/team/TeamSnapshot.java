package dev.patric.commonlib.api.team;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot of teams for a match.
 *
 * @param matchId match identifier.
 * @param teams team map (teamId -> members).
 * @param friendlyFirePolicy friendly-fire policy.
 */
public record TeamSnapshot(UUID matchId, Map<String, Set<UUID>> teams, FriendlyFirePolicy friendlyFirePolicy) {

    /**
     * Creates a team snapshot.
     */
    public TeamSnapshot {
        matchId = Objects.requireNonNull(matchId, "matchId");
        friendlyFirePolicy = Objects.requireNonNull(friendlyFirePolicy, "friendlyFirePolicy");
        teams = deepCopy(Objects.requireNonNull(teams, "teams"));
    }

    private static Map<String, Set<UUID>> deepCopy(Map<String, Set<UUID>> source) {
        Map<String, Set<UUID>> copy = new HashMap<>();
        for (Map.Entry<String, Set<UUID>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }
}
