package dev.patric.commonlib.adapter.fancyholograms;

import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FancyHologramsPortTest {

    @Test
    void createUpdateMoveDeleteLifecycleIsDeterministic() {
        FancyHologramsPort port = new FancyHologramsPort();
        Location start = new Location(null, 10, 64, 10);

        UUID id = port.create("spawn", start, List.of("Line1", "Line2"));
        assertNotNull(id);

        assertTrue(port.updateLines(id, List.of("Updated")));
        assertTrue(port.move(id, new Location(null, 12, 64, 12)));
        assertTrue(port.delete(id));
        assertFalse(port.delete(id));
    }
}
