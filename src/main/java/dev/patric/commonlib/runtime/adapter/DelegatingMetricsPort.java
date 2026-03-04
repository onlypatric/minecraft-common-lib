package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.port.MetricsPort;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Metrics port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingMetricsPort implements MetricsPort {

    private final MetricsPort fallback;
    private final AtomicReference<MetricsPort> delegate;

    /**
     * Creates a delegating metrics port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingMetricsPort(MetricsPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(MetricsPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public boolean initialize(JavaPlugin plugin, int pluginId) {
        return delegate.get().initialize(plugin, pluginId);
    }

    @Override
    public boolean addSimplePie(String chartId, Supplier<String> supplier) {
        return delegate.get().addSimplePie(chartId, supplier);
    }

    @Override
    public boolean addSingleLineChart(String chartId, IntSupplier supplier) {
        return delegate.get().addSingleLineChart(chartId, supplier);
    }

    @Override
    public void shutdown() {
        delegate.get().shutdown();
    }
}
