package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Number range input specification.
 *
 * @param key input key.
 * @param width width in range [1,1024].
 * @param label input label.
 * @param labelFormat label format template.
 * @param start range start.
 * @param end range end.
 * @param initial optional initial value.
 * @param step optional step value (>0).
 */
public record NumberRangeInputSpec(
        String key,
        int width,
        Component label,
        String labelFormat,
        float start,
        float end,
        @Nullable Float initial,
        @Nullable Float step
) implements DialogInputSpec {

    /**
     * Compact constructor validation.
     */
    public NumberRangeInputSpec {
        key = Objects.requireNonNull(key, "key").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (width < 1 || width > 1024) {
            throw new IllegalArgumentException("width must be in range [1,1024]");
        }
        label = Objects.requireNonNull(label, "label");
        labelFormat = Objects.requireNonNull(labelFormat, "labelFormat");
        if (end < start) {
            throw new IllegalArgumentException("end must be >= start");
        }
        if (initial != null && (initial < start || initial > end)) {
            throw new IllegalArgumentException("initial must be inside [start,end]");
        }
        if (step != null && step <= 0f) {
            throw new IllegalArgumentException("step must be > 0");
        }
    }
}
