package dev.patric.commonlib.api.dialog;

/**
 * Dialog session lifecycle states.
 */
public enum DialogSessionStatus {
    OPEN,
    CLOSING,
    CLOSED,
    TIMED_OUT,
    ERROR
}
