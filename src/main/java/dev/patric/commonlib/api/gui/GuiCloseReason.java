package dev.patric.commonlib.api.gui;

/**
 * Reasons that close a GUI session.
 */
public enum GuiCloseReason {
    USER_CLOSE,
    TIMEOUT,
    DISCONNECT,
    PLUGIN_DISABLE,
    REPLACED,
    ERROR
}
