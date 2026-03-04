package dev.patric.commonlib.api.dialog;

/**
 * Result of processing a dialog event.
 */
public enum DialogEventResult {
    APPLIED,
    DENIED_BY_POLICY,
    SESSION_NOT_FOUND,
    SESSION_NOT_OPEN,
    INVALID_PAYLOAD,
    HANDLER_ERROR
}
