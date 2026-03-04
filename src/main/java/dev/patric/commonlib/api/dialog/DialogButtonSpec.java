package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Action button specification.
 *
 * @param label button label.
 * @param tooltip optional tooltip.
 * @param width button width in range [1,1024].
 * @param action optional button action.
 */
public record DialogButtonSpec(
        Component label,
        @Nullable Component tooltip,
        int width,
        @Nullable DialogActionSpec action
) {

    /**
     * Compact constructor validation.
     */
    public DialogButtonSpec {
        label = Objects.requireNonNull(label, "label");
        if (width < 1 || width > 1024) {
            throw new IllegalArgumentException("width must be in range [1,1024]");
        }
    }
}
