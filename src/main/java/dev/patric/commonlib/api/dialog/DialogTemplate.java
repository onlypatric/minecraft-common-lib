package dev.patric.commonlib.api.dialog;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable dialog template model.
 *
 * @param templateKey template key.
 * @param base base dialog specification.
 * @param type dialog type specification.
 * @param metadata optional metadata.
 */
public record DialogTemplate(
        String templateKey,
        DialogBaseSpec base,
        DialogTypeSpec type,
        Map<String, String> metadata
) {

    /**
     * Compact constructor validation.
     */
    public DialogTemplate {
        templateKey = Objects.requireNonNull(templateKey, "templateKey").trim();
        if (templateKey.isEmpty()) {
            throw new IllegalArgumentException("templateKey must not be blank");
        }
        base = Objects.requireNonNull(base, "base");
        type = Objects.requireNonNull(type, "type");
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }
}
