package dev.patric.commonlib.api.hud;

/**
 * Result for bossbar update operations.
 */
public enum BossBarUpdateResult {
    APPLIED,
    THROTTLED,
    DEDUPED,
    BAR_NOT_FOUND,
    BAR_CLOSED,
    INVALID_STATE
}
