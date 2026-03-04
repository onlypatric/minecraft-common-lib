package dev.patric.commonlib.adapter.bstats;

import dev.patric.commonlib.api.port.MetricsPort;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * bStats-backed metrics port.
 */
public final class BStatsMetricsPort implements MetricsPort {

    private final AtomicReference<Object> metricsRef = new AtomicReference<>();

    @Override
    public boolean initialize(JavaPlugin plugin, int pluginId) {
        if (plugin == null || pluginId <= 0) {
            return false;
        }

        try {
            Class<?> metricsClass = Class.forName("org.bstats.bukkit.Metrics");
            Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class).newInstance(plugin, pluginId);
            metricsRef.set(metrics);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean addSimplePie(String chartId, Supplier<String> supplier) {
        Objects.requireNonNull(chartId, "chartId");
        Objects.requireNonNull(supplier, "supplier");

        Object metrics = metricsRef.get();
        if (metrics == null) {
            return false;
        }

        try {
            Class<?> callableClass = Class.forName("java.util.concurrent.Callable");
            Object callable = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{callableClass},
                    (proxy, method, args) -> {
                        if ("call".equals(method.getName())) {
                            return supplier.get();
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
            Class<?> simplePieClass = Class.forName("org.bstats.bukkit.Metrics$SimplePie");
            Object chart = simplePieClass.getConstructor(String.class, callableClass).newInstance(chartId, callable);
            invokeAddCustomChart(metrics, chart);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean addSingleLineChart(String chartId, IntSupplier supplier) {
        Objects.requireNonNull(chartId, "chartId");
        Objects.requireNonNull(supplier, "supplier");

        Object metrics = metricsRef.get();
        if (metrics == null) {
            return false;
        }

        try {
            Class<?> callableClass = Class.forName("java.util.concurrent.Callable");
            Object callable = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{callableClass},
                    (proxy, method, args) -> {
                        if ("call".equals(method.getName())) {
                            return supplier.getAsInt();
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
            Class<?> lineChartClass = Class.forName("org.bstats.bukkit.Metrics$SingleLineChart");
            Object chart = lineChartClass.getConstructor(String.class, callableClass).newInstance(chartId, callable);
            invokeAddCustomChart(metrics, chart);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public void shutdown() {
        metricsRef.set(null);
    }

    private static void invokeAddCustomChart(Object metrics, Object chart) throws ReflectiveOperationException {
        Class<?> chartClass = Class.forName("org.bstats.bukkit.charts.CustomChart");
        metrics.getClass().getMethod("addCustomChart", chartClass).invoke(metrics, chart);
    }
}
