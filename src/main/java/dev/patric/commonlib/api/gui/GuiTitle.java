package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Title descriptor for a GUI definition.
 *
 * @param value title value.
 */
public record GuiTitle(String value) {

    /**
     * Compact constructor validation.
     */
    public GuiTitle {
        value = Objects.requireNonNull(value, "value").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
