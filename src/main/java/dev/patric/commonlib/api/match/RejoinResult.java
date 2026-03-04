package dev.patric.commonlib.api.match;

/**
 * Rejoin operation result.
 */
public enum RejoinResult {
    REJOINED,
    DENIED_POLICY,
    DENIED_STATE,
    WINDOW_EXPIRED,
    SESSION_EXPIRED,
    NOT_PARTICIPANT,
    MATCH_NOT_FOUND,
    MATCH_CLOSED
}
