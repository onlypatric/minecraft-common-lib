package dev.patric.commonlib.api.dialog;

/**
 * Reasons used when closing dialog sessions.
 */
public enum DialogCloseReason {
    SUBMITTED,
    USER_CLOSE,
    TIMEOUT,
    QUIT,
    PLUGIN_DISABLE,
    REPLACED,
    ERROR
}
