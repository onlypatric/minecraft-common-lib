package dev.patric.commonlib.api.gui;

/**
 * Typed GUI action model.
 */
public sealed interface GuiAction
        permits RunCommandAction, OpenDialogAction, UpdateStateAction, CloseGuiAction, CustomAction,
        ToggleStateAction, OpenSubMenuAction, BackMenuAction {
}
