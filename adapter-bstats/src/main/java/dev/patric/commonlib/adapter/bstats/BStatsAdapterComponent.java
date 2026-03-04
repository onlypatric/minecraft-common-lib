package dev.patric.commonlib.adapter.bstats;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.MetricsPort;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Optional adapter component that binds bStats metrics backend.
 */
public final class BStatsAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-bstats";
    /** Library version metadata. */
    public static final String BACKEND_VERSION = "3.1.0";

    private final int pluginId;
    private final Supplier<MetricsPort> portFactory;

    /**
     * Creates component with plugin id and default backend factory.
     *
     * @param pluginId bStats plugin id.
     */
    public BStatsAdapterComponent(int pluginId) {
        this(pluginId, BStatsMetricsPort::new);
    }

    /**
     * Creates component with injected factory.
     *
     * @param pluginId bStats plugin id.
     * @param portFactory metrics port factory.
     */
    public BStatsAdapterComponent(int pluginId, Supplier<MetricsPort> portFactory) {
        this.pluginId = pluginId;
        this.portFactory = Objects.requireNonNull(portFactory, "portFactory");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEnable(CommonContext context) {
        PortBindingService bindingService = context.services().require(PortBindingService.class);

        if (pluginId <= 0) {
            bindingService.markUnavailable(StandardCapabilities.METRICS, "binding-failed:bstats:invalid-plugin-id");
            return;
        }

        MetricsPort metricsPort;
        try {
            metricsPort = portFactory.get();
            if (!metricsPort.initialize(context.plugin(), pluginId)) {
                bindingService.markUnavailable(StandardCapabilities.METRICS, "binding-failed:bstats:initialize");
                return;
            }
            bindingService.bindMetricsPort(metricsPort, "bstats", BACKEND_VERSION);
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.METRICS,
                    "binding-failed:bstats:" + ex.getClass().getSimpleName()
            );
        }
    }

    @Override
    public void onDisable(CommonContext context) {
        context.services().find(MetricsPort.class).ifPresent(MetricsPort::shutdown);
    }
}
