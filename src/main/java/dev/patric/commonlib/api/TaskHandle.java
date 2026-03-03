package dev.patric.commonlib.api;

/**
 * Handle for scheduled work owned by runtime scheduler.
 */
public interface TaskHandle {

    /**
     * Cancels underlying task.
     */
    void cancel();

    /**
     * Indicates whether task has been cancelled.
     *
     * @return true if cancelled.
     */
    boolean isCancelled();
}
