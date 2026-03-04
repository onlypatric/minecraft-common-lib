package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopArenaResetPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NoopPortsBehaviorTest {

    @Test
    void noopNpcPortHasDeterministicSafeBehavior() {
        NoopNpcPort port = new NoopNpcPort();
        Location location = new Location(null, 1, 2, 3);
        UUID id = port.spawn("template", location, "DemoNPC");

        assertNotNull(id);
        assertFalse(port.updateDisplayName(id, "New"));
        assertFalse(port.teleport(id, location));
        assertFalse(port.despawn(id));
    }

    @Test
    void noopHologramPortHasDeterministicSafeBehavior() {
        NoopHologramPort port = new NoopHologramPort();
        Location location = new Location(null, 4, 5, 6);
        UUID id = port.create("welcome", location, List.of("line-1", "line-2"));

        assertNotNull(id);
        assertFalse(port.updateLines(id, List.of("updated")));
        assertFalse(port.move(id, location));
        assertFalse(port.delete(id));
    }

    @Test
    void noopClaimsPortHasDeterministicSafeBehavior() {
        NoopClaimsPort port = new NoopClaimsPort();
        Location location = new Location(null, 7, 8, 9);
        UUID playerId = UUID.randomUUID();

        assertFalse(port.isInsideClaim(playerId, location));
        assertEquals(Optional.empty(), port.claimIdAt(location));
        assertFalse(port.hasBuildPermission(playerId, "claim-1"));
        assertFalse(port.hasCombatPermission(playerId, "claim-1"));
    }

    @Test
    void noopSchematicPortCompletesFuturesSuccessfully() {
        NoopSchematicPort port = new NoopSchematicPort();
        Location location = new Location(null, 10, 11, 12);
        PasteOptions options = PasteOptions.defaults();

        CompletableFuture<Void> paste = assertDoesNotThrow(() -> port.paste("map_a", location, options));
        CompletableFuture<Void> reset = assertDoesNotThrow(() -> port.resetRegion("arena_1", "template_a", options));

        assertNull(paste.join());
        assertNull(reset.join());
    }

    @Test
    void noopArenaResetPortCompletesFutureSuccessfully() {
        NoopArenaResetPort port = new NoopArenaResetPort();
        CompletableFuture<Void> reset = assertDoesNotThrow(() -> port.resetArena("arena-1"));
        assertNull(reset.join());
    }
}
