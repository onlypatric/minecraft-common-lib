package dev.patric.commonlib.api.gui;

/**
 * Result for GUI event dispatch operations.
 */
public enum GuiEventResult {
    APPLIED,
    DENIED_BY_POLICY,
    STALE_REVISION,
    SESSION_NOT_FOUND,
    SESSION_NOT_OPEN
}
