package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Immutable GUI layout descriptor.
 *
 * @param type logical GUI type.
 * @param size slot size.
 */
public record GuiLayout(GuiType type, int size) {

    /**
     * Compact constructor validation.
     */
    public GuiLayout {
        type = Objects.requireNonNull(type, "type");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
    }

    /**
     * Creates a standard chest layout by row count.
     *
     * @param rows chest rows (1..6).
     * @return chest layout.
     */
    public static GuiLayout chestRows(int rows) {
        return switch (rows) {
            case 1 -> new GuiLayout(GuiType.CHEST_9X1, GuiType.CHEST_9X1.size());
            case 2 -> new GuiLayout(GuiType.CHEST_9X2, GuiType.CHEST_9X2.size());
            case 3 -> new GuiLayout(GuiType.CHEST_9X3, GuiType.CHEST_9X3.size());
            case 4 -> new GuiLayout(GuiType.CHEST_9X4, GuiType.CHEST_9X4.size());
            case 5 -> new GuiLayout(GuiType.CHEST_9X5, GuiType.CHEST_9X5.size());
            case 6 -> new GuiLayout(GuiType.CHEST_9X6, GuiType.CHEST_9X6.size());
            default -> throw new IllegalArgumentException("rows must be between 1 and 6");
        };
    }
}
