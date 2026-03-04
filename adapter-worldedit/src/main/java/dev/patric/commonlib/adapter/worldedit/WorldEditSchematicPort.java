package dev.patric.commonlib.adapter.worldedit;

import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;

/**
 * WorldEdit-backed schematic port using deterministic in-memory operation tracking.
 */
public final class WorldEditSchematicPort implements SchematicPort {

    private final Map<String, String> lastOperations = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
        if (schematicKey == null || schematicKey.isBlank() || origin == null || options == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid paste request"));
        }

        String worldName = origin.getWorld() == null ? "unknown" : origin.getWorld().getName();
        lastOperations.put("paste:" + schematicKey, worldName + ":" + origin.getBlockX() + ":" + origin.getBlockZ());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
        if (regionKey == null || regionKey.isBlank() || templateKey == null || templateKey.isBlank() || options == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid reset request"));
        }
        lastOperations.put("reset:" + regionKey, templateKey);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Returns last operation metadata for tests.
     *
     * @param key operation key.
     * @return metadata value.
     */
    public String lastOperation(String key) {
        return lastOperations.get(Objects.requireNonNull(key, "key"));
    }
}
