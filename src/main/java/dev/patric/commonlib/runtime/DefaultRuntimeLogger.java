package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.RuntimeLogger;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default runtime logger implementation with shared prefix and level mapping.
 */
public final class DefaultRuntimeLogger implements RuntimeLogger {

    private static final String PREFIX = "[common-lib]";

    private final Logger logger;

    /**
     * Creates a runtime logger bound to a java.util logger.
     *
     * @param logger backing logger.
     */
    public DefaultRuntimeLogger(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public void lifecycleEvent(String phase, String componentId) {
        debug(phase + " -> " + componentId);
    }

    @Override
    public void debug(String message) {
        logger.fine(format(message));
    }

    @Override
    public void info(String message) {
        logger.info(format(message));
    }

    @Override
    public void warn(String message) {
        logger.warning(format(message));
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, format(message), throwable);
    }

    private String format(String message) {
        return PREFIX + " " + message;
    }
}
