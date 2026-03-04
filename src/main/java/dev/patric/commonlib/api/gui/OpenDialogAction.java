package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Dialog opening action.
 *
 * @param dialogTemplateKey dialog template key.
 * @param mapping mapping options.
 */
public record OpenDialogAction(String dialogTemplateKey, DialogOpenOptionsMapping mapping) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public OpenDialogAction {
        dialogTemplateKey = Objects.requireNonNull(dialogTemplateKey, "dialogTemplateKey").trim();
        if (dialogTemplateKey.isEmpty()) {
            throw new IllegalArgumentException("dialogTemplateKey must not be blank");
        }
        mapping = mapping == null ? DialogOpenOptionsMapping.defaults() : mapping;
    }
}
