package dev.patric.commonlib.api.gui;

/**
 * Result for state update operations.
 */
public enum GuiUpdateResult {
    APPLIED,
    STALE_REVISION,
    SESSION_NOT_FOUND,
    SESSION_NOT_OPEN
}
