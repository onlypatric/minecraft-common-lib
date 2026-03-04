package dev.patric.commonlib.api.match;

/**
 * Transition outcome for state changes.
 */
public enum MatchTransitionResult {
    APPLIED,
    INVALID_TRANSITION,
    MATCH_NOT_FOUND,
    MATCH_CLOSED
}
