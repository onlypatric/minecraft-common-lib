package dev.patric.commonlib.api.error;

import java.util.Objects;

/**
 * Structured error payload used by {@link OperationResult}.
 */
public final class OperationError {

    private final ErrorCodes code;
    private final String message;
    private final Throwable cause;

    private OperationError(ErrorCodes code, String message, Throwable cause) {
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.cause = cause;
    }

    /**
     * Creates a structured error without throwable cause.
     *
     * @param code error code.
     * @param message error message.
     * @return error instance.
     */
    public static OperationError of(ErrorCodes code, String message) {
        return new OperationError(code, message, null);
    }

    /**
     * Creates a structured error with throwable cause.
     *
     * @param code error code.
     * @param message error message.
     * @param cause underlying cause.
     * @return error instance.
     */
    public static OperationError of(ErrorCodes code, String message, Throwable cause) {
        return new OperationError(code, message, cause);
    }

    /**
     * Returns error code.
     *
     * @return code.
     */
    public ErrorCodes code() {
        return code;
    }

    /**
     * Returns error message.
     *
     * @return message.
     */
    public String message() {
        return message;
    }

    /**
     * Returns throwable cause, if present.
     *
     * @return cause or null.
     */
    public Throwable cause() {
        return cause;
    }
}
