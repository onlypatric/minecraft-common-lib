package dev.patric.commonlib.api.gui;

/**
 * Current lifecycle status of a GUI session.
 */
public enum GuiSessionStatus {
    OPEN,
    CLOSING,
    CLOSED,
    TIMED_OUT
}
