package dev.patric.commonlib.api.command;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.ServiceRegistry;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Execution context for command model handlers.
 */
public interface CommandContext {

    /**
     * Sender id.
     *
     * @return sender UUID.
     */
    UUID senderId();

    /**
     * Sender locale.
     *
     * @return locale.
     */
    Locale locale();

    /**
     * Parsed argument map.
     *
     * @return args map.
     */
    Map<String, Object> args();

    /**
     * Shared service registry.
     *
     * @return services.
     */
    ServiceRegistry services();

    /**
     * Shared scheduler facade.
     *
     * @return scheduler.
     */
    CommonScheduler scheduler();
}
