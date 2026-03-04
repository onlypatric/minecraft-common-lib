package dev.patric.commonlib.adapter.fancynpcs;

import java.util.UUID;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FancyNpcsPortTest {

    @Test
    void spawnUpdateTeleportDespawnLifecycleIsDeterministic() {
        FancyNpcsPort port = new FancyNpcsPort();
        Location start = new Location(null, 20, 70, 20);

        UUID id = port.spawn("villager", start, "Trader");
        assertNotNull(id);

        assertTrue(port.updateDisplayName(id, "Blacksmith"));
        assertTrue(port.teleport(id, new Location(null, 21, 70, 21)));
        assertTrue(port.despawn(id));
        assertFalse(port.despawn(id));
    }
}
