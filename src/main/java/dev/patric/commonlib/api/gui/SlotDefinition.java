package dev.patric.commonlib.api.gui;

import java.util.List;
import java.util.Objects;

/**
 * Slot descriptor in a GUI definition.
 *
 * @param slot slot index.
 * @param item optional visual item descriptor.
 * @param interaction interaction policy.
 * @param actions bound actions.
 */
public record SlotDefinition(
        int slot,
        GuiItemView item,
        SlotInteractionPolicy interaction,
        List<GuiAction> actions
) {

    /**
     * Compact constructor validation.
     */
    public SlotDefinition {
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be >= 0");
        }
        interaction = Objects.requireNonNull(interaction, "interaction");
        actions = List.copyOf(actions == null ? List.of() : actions);
    }
}
