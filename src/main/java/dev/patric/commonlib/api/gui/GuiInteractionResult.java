package dev.patric.commonlib.api.gui;

/**
 * Result of GUI interaction pipeline.
 */
public enum GuiInteractionResult {
    APPLIED,
    DENIED_BY_POLICY,
    INVALID_ACTION,
    STALE_REVISION,
    SESSION_NOT_FOUND,
    SESSION_NOT_OPEN
}
