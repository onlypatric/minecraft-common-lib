package dev.patric.commonlib.api.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable GUI definition used by runtime sessions.
 *
 * @param key definition key.
 * @param layout layout descriptor.
 * @param title title descriptor.
 * @param slots slot definitions.
 * @param behaviorPolicy global behavior policy.
 */
public record GuiDefinition(
        String key,
        GuiLayout layout,
        GuiTitle title,
        Map<Integer, SlotDefinition> slots,
        GuiBehaviorPolicy behaviorPolicy
) {

    /**
     * Compact constructor validation.
     */
    public GuiDefinition {
        key = Objects.requireNonNull(key, "key").trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        layout = Objects.requireNonNull(layout, "layout");
        title = Objects.requireNonNull(title, "title");
        behaviorPolicy = behaviorPolicy == null ? GuiBehaviorPolicy.strictDefaults() : behaviorPolicy;

        Map<Integer, SlotDefinition> copy = new LinkedHashMap<>();
        for (Map.Entry<Integer, SlotDefinition> entry : Objects.requireNonNull(slots, "slots").entrySet()) {
            Integer slot = Objects.requireNonNull(entry.getKey(), "slot key");
            SlotDefinition definition = Objects.requireNonNull(entry.getValue(), "slot definition");
            if (slot < 0 || slot >= layout.size()) {
                throw new IllegalArgumentException("slot out of layout bounds: " + slot + " for size " + layout.size());
            }
            copy.put(slot, definition);
        }
        slots = Map.copyOf(copy);
    }

    /**
     * Creates a minimal chest GUI definition.
     *
     * @param key definition key.
     * @param rows chest rows (1..6).
     * @param title title text.
     * @return minimal definition.
     */
    public static GuiDefinition chest(String key, int rows, String title) {
        return new GuiDefinition(
                key,
                GuiLayout.chestRows(rows),
                new GuiTitle(title),
                Map.of(),
                GuiBehaviorPolicy.strictDefaults()
        );
    }
}
