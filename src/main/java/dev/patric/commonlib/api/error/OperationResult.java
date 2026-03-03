package dev.patric.commonlib.api.error;

import java.util.Objects;

/**
 * Generic success/failure wrapper for helper utilities.
 *
 * @param <T> success value type.
 */
public final class OperationResult<T> {

    private final T value;
    private final OperationError error;

    private OperationResult(T value, OperationError error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a success result.
     *
     * @param value success value.
     * @param <T> value type.
     * @return success result.
     */
    public static <T> OperationResult<T> success(T value) {
        return new OperationResult<>(value, null);
    }

    /**
     * Creates a failure result.
     *
     * @param error error payload.
     * @param <T> value type.
     * @return failure result.
     */
    public static <T> OperationResult<T> failure(OperationError error) {
        return new OperationResult<>(null, Objects.requireNonNull(error, "error"));
    }

    /**
     * Returns true for success result.
     *
     * @return true if success.
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Returns true for failure result.
     *
     * @return true if failure.
     */
    public boolean isFailure() {
        return error != null;
    }

    /**
     * Returns success value or null.
     *
     * @return value or null.
     */
    public T valueOrNull() {
        return value;
    }

    /**
     * Returns failure payload or null.
     *
     * @return error or null.
     */
    public OperationError errorOrNull() {
        return error;
    }
}
