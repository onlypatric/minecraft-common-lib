package dev.patric.commonlib.adapter.huskclaims;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HuskClaimsClaimsPortTest {

    @Test
    void territorySafePolicyNeverThrowsAndReturnsDeterministicDefaults() {
        HuskClaimsClaimsPort port = new HuskClaimsClaimsPort();

        assertFalse(port.isInsideClaim(null, null));
        assertEquals(Optional.empty(), port.claimIdAt(null));
        assertFalse(port.hasBuildPermission(null, null));
        assertFalse(port.hasCombatPermission(null, null));
        assertFalse(port.hasBuildPermission(UUID.randomUUID(), "protected:spawn"));
        assertFalse(port.hasCombatPermission(UUID.randomUUID(), "safezone:lobby"));
    }

    @Test
    void claimIdDerivationUsesChunkCoordinates() {
        HuskClaimsClaimsPort port = new HuskClaimsClaimsPort();
        World world = mock(World.class);
        when(world.getName()).thenReturn("arena_world");

        Location location = new Location(world, 33, 64, -17);

        Optional<String> claimId = port.claimIdAt(location);

        assertTrue(claimId.isPresent());
        assertEquals("arena_world:2:-2", claimId.orElseThrow());
        assertTrue(port.isInsideClaim(UUID.randomUUID(), location));
    }
}
