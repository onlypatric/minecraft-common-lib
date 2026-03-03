package dev.patric.commonlib.plugin;

/**
 * Standardized lifecycle contract for Bukkit/Paper plugins.
 *
 * @deprecated Prefer {@code dev.patric.commonlib.api.CommonComponent}.
 */
@Deprecated(since = "0.1.0", forRemoval = false)
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
