package dev.patric.commonlib.api.port;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

/**
 * Claims integration port.
 */
public interface ClaimsPort {

    /**
     * Checks whether player is inside a claim at location.
     *
     * @param playerId player id.
     * @param location location.
     * @return true when player is in a claim.
     */
    boolean isInsideClaim(UUID playerId, Location location);

    /**
     * Resolves claim id at location.
     *
     * @param location location.
     * @return claim id if found.
     */
    Optional<String> claimIdAt(Location location);

    /**
     * Checks build permission inside a claim.
     *
     * @param playerId player id.
     * @param claimId claim id.
     * @return true when build is allowed.
     */
    boolean hasBuildPermission(UUID playerId, String claimId);

    /**
     * Checks combat permission inside a claim.
     *
     * @param playerId player id.
     * @param claimId claim id.
     * @return true when combat is allowed.
     */
    boolean hasCombatPermission(UUID playerId, String claimId);
}
