package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Action that toggles one boolean-like value in GUI state and swaps slot item view.
 *
 * @param stateKey target GUI state key.
 * @param onValue string value used for enabled state.
 * @param offValue string value used for disabled state.
 * @param onItem item view rendered for enabled state.
 * @param offItem item view rendered for disabled state.
 * @param rerender whether full GUI rerender should be forced after toggle.
 */
public record ToggleStateAction(
        String stateKey,
        String onValue,
        String offValue,
        GuiItemView onItem,
        GuiItemView offItem,
        boolean rerender
) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public ToggleStateAction {
        stateKey = Objects.requireNonNull(stateKey, "stateKey").trim();
        onValue = Objects.requireNonNull(onValue, "onValue").trim();
        offValue = Objects.requireNonNull(offValue, "offValue").trim();
        onItem = Objects.requireNonNull(onItem, "onItem");
        offItem = Objects.requireNonNull(offItem, "offItem");
        if (stateKey.isEmpty()) {
            throw new IllegalArgumentException("stateKey must not be blank");
        }
        if (onValue.isEmpty()) {
            throw new IllegalArgumentException("onValue must not be blank");
        }
        if (offValue.isEmpty()) {
            throw new IllegalArgumentException("offValue must not be blank");
        }
    }
}
