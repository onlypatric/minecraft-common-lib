package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.ServiceRegistry;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Default immutable runtime context.
 *
 * @param plugin owning plugin.
 * @param logger runtime logger.
 * @param scheduler scheduler facade.
 * @param services service registry.
 */
public record DefaultCommonContext(
        JavaPlugin plugin,
        Logger logger,
        CommonScheduler scheduler,
        ServiceRegistry services
) implements CommonContext {

    /**
     * Creates a validated runtime context.
     *
     * @param plugin owning plugin.
     * @param logger runtime logger.
     * @param scheduler scheduler facade.
     * @param services service registry.
     */
    public DefaultCommonContext {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(logger, "logger");
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(services, "services");
    }
}
