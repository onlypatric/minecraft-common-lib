package dev.patric.commonlib.api.gui;

/**
 * Supported high-level GUI container types.
 */
public enum GuiType {
    CHEST_9X1(9),
    CHEST_9X2(18),
    CHEST_9X3(27),
    CHEST_9X4(36),
    CHEST_9X5(45),
    CHEST_9X6(54);

    private final int size;

    GuiType(int size) {
        this.size = size;
    }

    /**
     * Returns container size in slots.
     *
     * @return slot size.
     */
    public int size() {
        return size;
    }
}
