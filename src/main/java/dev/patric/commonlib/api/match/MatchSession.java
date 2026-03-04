package dev.patric.commonlib.api.match;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable snapshot for a match session.
 *
 * @param matchId match identifier.
 * @param matchKey match key.
 * @param state current state.
 * @param status session status.
 * @param policy session policy.
 * @param connectedPlayers connected participants.
 * @param disconnectedPlayers disconnected participants tracked for rejoin.
 * @param createdAtEpochMilli creation timestamp.
 * @param stateEnteredTick engine tick when current state started.
 * @param currentTick latest observed engine tick.
 * @param lastEndReason last end reason if present.
 */
public record MatchSession(
        UUID matchId,
        String matchKey,
        MatchState state,
        MatchSessionStatus status,
        MatchPolicy policy,
        Set<UUID> connectedPlayers,
        Set<UUID> disconnectedPlayers,
        long createdAtEpochMilli,
        long stateEnteredTick,
        long currentTick,
        EndReason lastEndReason
) {

    /**
     * Creates an immutable match snapshot.
     */
    public MatchSession {
        matchId = Objects.requireNonNull(matchId, "matchId");
        matchKey = Objects.requireNonNull(matchKey, "matchKey");
        state = Objects.requireNonNull(state, "state");
        status = Objects.requireNonNull(status, "status");
        policy = Objects.requireNonNull(policy, "policy");
        connectedPlayers = Set.copyOf(Objects.requireNonNull(connectedPlayers, "connectedPlayers"));
        disconnectedPlayers = Set.copyOf(Objects.requireNonNull(disconnectedPlayers, "disconnectedPlayers"));
    }
}
