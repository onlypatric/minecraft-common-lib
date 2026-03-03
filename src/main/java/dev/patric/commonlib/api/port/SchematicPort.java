package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

/**
 * Schematic/region reset integration port.
 */
public interface SchematicPort {

    /**
     * Pastes a schematic at an origin location.
     *
     * @param schematicKey schematic/template key.
     * @param origin origin location.
     * @param options paste options.
     * @return completion future.
     */
    CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options);

    /**
     * Resets a region from a template.
     *
     * @param regionKey region identifier.
     * @param templateKey template identifier.
     * @param options paste options.
     * @return completion future.
     */
    CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options);
}
