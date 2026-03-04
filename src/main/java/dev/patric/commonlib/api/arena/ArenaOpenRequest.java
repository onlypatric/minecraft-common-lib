package dev.patric.commonlib.api.arena;

import java.util.Map;
import java.util.Objects;

/**
 * Request used to open an arena instance.
 *
 * @param arenaId arena identifier.
 * @param templateKey template key for reset strategy.
 * @param worldKey world key reference.
 * @param resetStrategyKey strategy key used by arena service.
 * @param metadata free-form metadata.
 */
public record ArenaOpenRequest(
        String arenaId,
        String templateKey,
        String worldKey,
        String resetStrategyKey,
        Map<String, String> metadata
) {

    /**
     * Creates an arena open request.
     */
    public ArenaOpenRequest {
        arenaId = requireText(arenaId, "arenaId");
        templateKey = requireText(templateKey, "templateKey");
        worldKey = requireText(worldKey, "worldKey");
        resetStrategyKey = requireText(resetStrategyKey, "resetStrategyKey");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
