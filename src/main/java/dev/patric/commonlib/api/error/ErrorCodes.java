package dev.patric.commonlib.api.error;

/**
 * Standard error codes for common helper operations.
 */
public enum ErrorCodes {
    /** Invalid input or invariant violation. */
    VALIDATION_ERROR,
    /** Requested dependency/service is not available. */
    MISSING_SERVICE,
    /** Operation attempted in an invalid lifecycle/runtime state. */
    ILLEGAL_STATE,
    /** Unexpected internal failure. */
    INTERNAL_ERROR
}
