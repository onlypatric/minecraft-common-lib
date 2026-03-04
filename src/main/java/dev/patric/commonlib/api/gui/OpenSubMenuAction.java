package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Action that opens another registered GUI definition as submenu.
 *
 * @param targetMenuKey target menu key resolved through {@link GuiDefinitionRegistry}.
 * @param inheritState whether current GUI state should be passed to target menu.
 * @param inheritPlaceholders whether current GUI placeholders should be passed to target menu.
 */
public record OpenSubMenuAction(
        String targetMenuKey,
        boolean inheritState,
        boolean inheritPlaceholders
) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public OpenSubMenuAction {
        targetMenuKey = Objects.requireNonNull(targetMenuKey, "targetMenuKey").trim();
        if (targetMenuKey.isEmpty()) {
            throw new IllegalArgumentException("targetMenuKey must not be blank");
        }
    }
}
