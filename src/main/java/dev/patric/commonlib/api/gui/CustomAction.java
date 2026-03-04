package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Custom action marker consumed by plugin-specific handlers.
 *
 * @param actionKey custom action key.
 */
public record CustomAction(String actionKey) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public CustomAction {
        actionKey = Objects.requireNonNull(actionKey, "actionKey").trim();
        if (actionKey.isEmpty()) {
            throw new IllegalArgumentException("actionKey must not be blank");
        }
    }
}
