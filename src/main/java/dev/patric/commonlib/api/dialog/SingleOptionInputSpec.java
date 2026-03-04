package dev.patric.commonlib.api.dialog;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;

/**
 * Single-choice input specification.
 *
 * @param key input key.
 * @param width width in range [1,1024].
 * @param label input label.
 * @param labelVisible whether label is visible.
 * @param entries available options.
 */
public record SingleOptionInputSpec(
        String key,
        int width,
        Component label,
        boolean labelVisible,
        List<SingleOptionEntrySpec> entries
) implements DialogInputSpec {

    /**
     * Compact constructor validation.
     */
    public SingleOptionInputSpec {
        key = Objects.requireNonNull(key, "key").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (width < 1 || width > 1024) {
            throw new IllegalArgumentException("width must be in range [1,1024]");
        }
        label = Objects.requireNonNull(label, "label");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries must not be empty");
        }
    }
}
