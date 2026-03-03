package dev.patric.commonlib.api;

/**
 * Standard runtime logger for consistent lifecycle and diagnostics output.
 */
public interface RuntimeLogger {

    /**
     * Standard message prefix used by runtime logs.
     *
     * @return prefix value.
     */
    String prefix();

    /**
     * Logs a lifecycle event for a component.
     *
     * @param phase lifecycle phase (onLoad/onEnable/onDisable).
     * @param componentId component identifier.
     */
    void lifecycleEvent(String phase, String componentId);

    /**
     * Logs a debug message.
     *
     * @param message debug message.
     */
    void debug(String message);

    /**
     * Logs an info message.
     *
     * @param message info message.
     */
    void info(String message);

    /**
     * Logs a warning message.
     *
     * @param message warning message.
     */
    void warn(String message);

    /**
     * Logs an error message.
     *
     * @param message error message.
     * @param throwable cause.
     */
    void error(String message, Throwable throwable);
}
