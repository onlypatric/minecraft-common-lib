package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.port.MetricsPort;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * No-op metrics port.
 */
public final class NoopMetricsPort implements MetricsPort {

    @Override
    public boolean initialize(JavaPlugin plugin, int pluginId) {
        Objects.requireNonNull(plugin, "plugin");
        return true;
    }

    @Override
    public boolean addSimplePie(String chartId, Supplier<String> supplier) {
        Objects.requireNonNull(chartId, "chartId");
        Objects.requireNonNull(supplier, "supplier");
        return true;
    }

    @Override
    public boolean addSingleLineChart(String chartId, IntSupplier supplier) {
        Objects.requireNonNull(chartId, "chartId");
        Objects.requireNonNull(supplier, "supplier");
        return true;
    }

    @Override
    public void shutdown() {
        // no-op
    }
}
