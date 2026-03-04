package dev.patric.commonlib.api.gui;

import java.util.List;
import java.util.Objects;

/**
 * Serializable item descriptor used by GUI renderers.
 *
 * @param materialKey material key (e.g. DIAMOND).
 * @param displayName display name.
 * @param lore lore lines.
 */
public record GuiItemView(
        String materialKey,
        String displayName,
        List<String> lore
) {

    /**
     * Compact constructor validation.
     */
    public GuiItemView {
        materialKey = Objects.requireNonNull(materialKey, "materialKey").trim();
        if (materialKey.isEmpty()) {
            throw new IllegalArgumentException("materialKey must not be blank");
        }
        displayName = Objects.requireNonNull(displayName, "displayName");
        lore = List.copyOf(lore == null ? List.of() : lore);
    }

    /**
     * Creates a minimal item view without lore.
     *
     * @param materialKey material key.
     * @param displayName display name.
     * @return item view.
     */
    public static GuiItemView of(String materialKey, String displayName) {
        return new GuiItemView(materialKey, displayName, List.of());
    }
}
