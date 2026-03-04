package dev.patric.commonlib.api.arena;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable arena instance snapshot.
 *
 * @param arenaId arena identifier.
 * @param templateKey template key used for resets.
 * @param worldKey world key reference.
 * @param metadata free-form metadata.
 * @param status arena status.
 */
public record ArenaInstance(
        String arenaId,
        String templateKey,
        String worldKey,
        Map<String, String> metadata,
        ArenaStatus status
) {

    /**
     * Creates an arena instance snapshot.
     */
    public ArenaInstance {
        arenaId = requireText(arenaId, "arenaId");
        templateKey = requireText(templateKey, "templateKey");
        worldKey = requireText(worldKey, "worldKey");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
        status = Objects.requireNonNull(status, "status");
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
