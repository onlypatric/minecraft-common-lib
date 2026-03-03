package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.port.ClaimsPort;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

/**
 * No-op claims port.
 */
public final class NoopClaimsPort implements ClaimsPort {

    @Override
    public boolean isInsideClaim(UUID playerId, Location location) {
        return false;
    }

    @Override
    public Optional<String> claimIdAt(Location location) {
        return Optional.empty();
    }

    @Override
    public boolean hasBuildPermission(UUID playerId, String claimId) {
        return false;
    }

    @Override
    public boolean hasCombatPermission(UUID playerId, String claimId) {
        return false;
    }
}
