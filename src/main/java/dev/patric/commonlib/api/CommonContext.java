package dev.patric.commonlib.api;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Immutable runtime context shared across components.
 */
public interface CommonContext {

    /**
     * Plugin instance hosting this runtime.
     *
     * @return plugin instance.
     */
    JavaPlugin plugin();

    /**
     * Logger associated with hosting plugin.
     *
     * @return logger.
     */
    Logger logger();

    /**
     * Shared scheduler abstraction.
     *
     * @return scheduler.
     */
    CommonScheduler scheduler();

    /**
     * Shared service registry.
     *
     * @return service registry.
     */
    ServiceRegistry services();
}
