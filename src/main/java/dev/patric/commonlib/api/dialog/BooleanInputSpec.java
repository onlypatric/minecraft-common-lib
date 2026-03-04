package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import net.kyori.adventure.text.Component;

/**
 * Boolean input specification.
 *
 * @param key input key.
 * @param label input label.
 * @param initial initial value.
 * @param onTrue command-template replacement for true.
 * @param onFalse command-template replacement for false.
 */
public record BooleanInputSpec(
        String key,
        Component label,
        boolean initial,
        String onTrue,
        String onFalse
) implements DialogInputSpec {

    /**
     * Compact constructor validation.
     */
    public BooleanInputSpec {
        key = Objects.requireNonNull(key, "key").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        label = Objects.requireNonNull(label, "label");
        onTrue = Objects.requireNonNull(onTrue, "onTrue");
        onFalse = Objects.requireNonNull(onFalse, "onFalse");
    }
}
