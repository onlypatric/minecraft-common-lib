package dev.patric.commonlib.adapter.worldedit;

import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.concurrent.ExecutionException;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorldEditSchematicPortTest {

    @Test
    void pasteAndResetRecordOperationsDeterministically() {
        WorldEditSchematicPort port = new WorldEditSchematicPort();
        World world = mock(World.class);
        when(world.getName()).thenReturn("arena");

        PasteOptions options = new PasteOptions(false, false, false, 5000);

        port.paste("spawn", new Location(world, 10, 70, 10), options).join();
        port.resetRegion("region-1", "template-1", options).join();

        assertEquals("arena:10:10", port.lastOperation("paste:spawn"));
        assertEquals("template-1", port.lastOperation("reset:region-1"));
    }

    @Test
    void invalidInputsReturnFailedFuture() {
        WorldEditSchematicPort port = new WorldEditSchematicPort();

        assertThrows(ExecutionException.class, () -> port.paste(null, null, null).get());
        assertThrows(ExecutionException.class, () -> port.resetRegion("", "", null).get());
    }
}
