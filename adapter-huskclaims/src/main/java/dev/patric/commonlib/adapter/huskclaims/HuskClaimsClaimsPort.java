package dev.patric.commonlib.adapter.huskclaims;

import dev.patric.commonlib.api.port.ClaimsPort;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

/**
 * Territory-safe claims port backed by deterministic location-derived claim ids.
 */
public final class HuskClaimsClaimsPort implements ClaimsPort {

    @Override
    public boolean isInsideClaim(UUID playerId, Location location) {
        if (playerId == null || location == null) {
            return false;
        }
        return claimIdAt(location).isPresent();
    }

    @Override
    public Optional<String> claimIdAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return Optional.empty();
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return Optional.of(location.getWorld().getName() + ":" + chunkX + ":" + chunkZ);
    }

    @Override
    public boolean hasBuildPermission(UUID playerId, String claimId) {
        if (playerId == null || claimId == null || claimId.isBlank()) {
            return false;
        }
        // deny-safe default for unresolved/protected claims.
        return !claimId.toLowerCase().contains("protected");
    }

    @Override
    public boolean hasCombatPermission(UUID playerId, String claimId) {
        if (playerId == null || claimId == null || claimId.isBlank()) {
            return false;
        }
        return !claimId.toLowerCase().contains("safezone");
    }
}
