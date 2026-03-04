package dev.patric.commonlib.adapter.fawe;

import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.concurrent.ExecutionException;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FaweSchematicPortTest {

    @Test
    void pasteAndResetRecordOperationsDeterministically() {
        FaweSchematicPort port = new FaweSchematicPort();
        World world = mock(World.class);
        when(world.getName()).thenReturn("arena");

        PasteOptions options = new PasteOptions(true, false, false, 20000);

        port.paste("spawn", new Location(world, 20, 70, -4), options).join();
        port.resetRegion("region-2", "template-2", options).join();

        assertEquals("arena:20:-4", port.lastOperation("paste:spawn"));
        assertEquals("template-2", port.lastOperation("reset:region-2"));
    }

    @Test
    void invalidInputsReturnFailedFuture() {
        FaweSchematicPort port = new FaweSchematicPort();

        assertThrows(ExecutionException.class, () -> port.paste("", null, null).get());
        assertThrows(ExecutionException.class, () -> port.resetRegion(null, null, null).get());
    }
}
