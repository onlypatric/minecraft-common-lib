package dev.patric.commonlib.plugin;

/**
 * Standardized lifecycle contract for Bukkit/Paper plugins.
 */
public interface PluginLifecycle {

    /**
     * Called when plugin enabling logic should run.
     */
    void onEnable();

    /**
     * Called when plugin disabling logic should run.
     */
    void onDisable();
}
