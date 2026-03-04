package dev.patric.commonlib.api.match;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Request used to open a new match session.
 *
 * @param matchKey match key.
 * @param policy match behavior policy.
 * @param callbacks lifecycle callbacks.
 * @param cleanup cleanup contract.
 * @param initialPlayers initial connected players.
 */
public record MatchOpenRequest(
        String matchKey,
        MatchPolicy policy,
        MatchCallbacks callbacks,
        MatchCleanup cleanup,
        Set<UUID> initialPlayers
) {

    /**
     * Creates a match open request.
     */
    public MatchOpenRequest {
        matchKey = Objects.requireNonNull(matchKey, "matchKey");
        if (matchKey.isBlank()) {
            throw new IllegalArgumentException("matchKey must not be blank");
        }
        policy = Objects.requireNonNull(policy, "policy");
        callbacks = Objects.requireNonNull(callbacks, "callbacks");
        cleanup = Objects.requireNonNull(cleanup, "cleanup");
        initialPlayers = Set.copyOf(Objects.requireNonNull(initialPlayers, "initialPlayers"));
    }
}
