package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Session close action.
 *
 * @param reason close reason.
 */
public record CloseGuiAction(GuiCloseReason reason) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public CloseGuiAction {
        reason = Objects.requireNonNull(reason, "reason");
    }
}
