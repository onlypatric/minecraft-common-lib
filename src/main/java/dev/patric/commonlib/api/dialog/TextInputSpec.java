package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Text input specification.
 *
 * @param key input key.
 * @param width width in range [1,1024].
 * @param label input label.
 * @param labelVisible whether label is visible.
 * @param initial initial text.
 * @param maxLength maximum text length (>0).
 * @param maxLines optional multiline max lines.
 * @param height optional multiline height in range [1,512].
 */
public record TextInputSpec(
        String key,
        int width,
        Component label,
        boolean labelVisible,
        String initial,
        int maxLength,
        @Nullable Integer maxLines,
        @Nullable Integer height
) implements DialogInputSpec {

    /**
     * Compact constructor validation.
     */
    public TextInputSpec {
        key = Objects.requireNonNull(key, "key").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (width < 1 || width > 1024) {
            throw new IllegalArgumentException("width must be in range [1,1024]");
        }
        label = Objects.requireNonNull(label, "label");
        initial = Objects.requireNonNull(initial, "initial");
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be > 0");
        }
        if (maxLines != null && maxLines <= 0) {
            throw new IllegalArgumentException("maxLines must be > 0");
        }
        if (height != null && (height < 1 || height > 512)) {
            throw new IllegalArgumentException("height must be in range [1,512]");
        }
    }
}
