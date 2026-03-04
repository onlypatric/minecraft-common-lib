package dev.patric.commonlib.api.team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Team service with assignment and friendly-fire helpers.
 */
public interface TeamService {

    /**
     * Creates or replaces roster for a match.
     *
     * @param matchId match identifier.
     * @param definitions team definitions.
     * @param policy friendly-fire policy.
     */
    void createRoster(UUID matchId, List<TeamDefinition> definitions, FriendlyFirePolicy policy);

    /**
     * Assigns player to a specific team.
     *
     * @param matchId match identifier.
     * @param playerId player identifier.
     * @param teamId team identifier.
     * @return assignment result.
     */
    TeamAssignmentResult assign(UUID matchId, UUID playerId, String teamId);

    /**
     * Auto-assigns player to a team.
     *
     * @param matchId match identifier.
     * @param playerId player identifier.
     * @return assignment result.
     */
    TeamAssignmentResult autoAssign(UUID matchId, UUID playerId);

    /**
     * Resolves player's team in a match.
     *
     * @param matchId match identifier.
     * @param playerId player identifier.
     * @return team id when assigned.
     */
    Optional<String> teamOf(UUID matchId, UUID playerId);

    /**
     * Checks if attacker can damage victim according to policy.
     *
     * @param matchId match identifier.
     * @param attackerId attacker player id.
     * @param victimId victim player id.
     * @return true when damage is allowed.
     */
    boolean canDamage(UUID matchId, UUID attackerId, UUID victimId);

    /**
     * Removes player assignment.
     *
     * @param matchId match identifier.
     * @param playerId player identifier.
     */
    void removePlayer(UUID matchId, UUID playerId);

    /**
     * Returns team snapshot for a match.
     *
     * @param matchId match identifier.
     * @return snapshot when roster exists.
     */
    Optional<TeamSnapshot> snapshot(UUID matchId);

    /**
     * Clears roster for a match.
     *
     * @param matchId match identifier.
     * @return true when roster existed.
     */
    boolean clearRoster(UUID matchId);
}
