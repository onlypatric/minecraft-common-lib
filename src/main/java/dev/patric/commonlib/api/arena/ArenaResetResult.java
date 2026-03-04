package dev.patric.commonlib.api.arena;

/**
 * Result of an arena reset request.
 */
public enum ArenaResetResult {
    APPLIED,
    THROTTLED,
    ARENA_NOT_FOUND,
    ARENA_DISPOSED,
    FAILED
}
