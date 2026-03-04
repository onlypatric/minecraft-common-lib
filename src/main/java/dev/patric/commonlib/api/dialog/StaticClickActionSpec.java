package dev.patric.commonlib.api.dialog;

import java.util.Objects;

/**
 * Static click action specification.
 *
 * @param kind action kind.
 * @param value action value.
 */
public record StaticClickActionSpec(StaticClickActionKind kind, String value) implements DialogActionSpec {

    /**
     * Compact constructor validation.
     */
    public StaticClickActionSpec {
        kind = Objects.requireNonNull(kind, "kind");
        value = Objects.requireNonNull(value, "value").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
