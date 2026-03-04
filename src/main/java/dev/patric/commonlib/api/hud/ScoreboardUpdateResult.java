package dev.patric.commonlib.api.hud;

/**
 * Result for scoreboard update operations.
 */
public enum ScoreboardUpdateResult {
    APPLIED,
    THROTTLED,
    DEDUPED,
    SESSION_NOT_FOUND,
    SESSION_CLOSED,
    INVALID_PAYLOAD
}
