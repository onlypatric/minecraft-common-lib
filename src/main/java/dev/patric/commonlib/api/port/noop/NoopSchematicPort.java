package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

/**
 * No-op schematic port.
 */
public final class NoopSchematicPort implements SchematicPort {

    @Override
    public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
        return CompletableFuture.completedFuture(null);
    }
}
