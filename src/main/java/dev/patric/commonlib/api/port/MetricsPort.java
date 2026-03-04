package dev.patric.commonlib.api.port;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Metrics integration port.
 */
public interface MetricsPort {

    /**
     * Initializes metrics backend.
     *
     * @param plugin owning plugin.
     * @param pluginId bStats plugin id.
     * @return true when initialization succeeded.
     */
    boolean initialize(JavaPlugin plugin, int pluginId);

    /**
     * Adds a simple pie chart.
     *
     * @param chartId chart identifier.
     * @param supplier value supplier.
     * @return true when chart registration succeeded.
     */
    boolean addSimplePie(String chartId, Supplier<String> supplier);

    /**
     * Adds a single line chart.
     *
     * @param chartId chart identifier.
     * @param supplier value supplier.
     * @return true when chart registration succeeded.
     */
    boolean addSingleLineChart(String chartId, IntSupplier supplier);

    /**
     * Shuts down metrics backend.
     */
    void shutdown();
}
