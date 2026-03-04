package dev.patric.commonlib.adapter.fancyholograms;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds FancyHolograms-backed hologram port when available.
 */
public final class FancyHologramsAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-fancyholograms";
    /** Minimum supported FancyHolograms version. */
    public static final String MINIMUM_VERSION = "2.9.1";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<HologramPort> portFactory;

    /**
     * Creates component with default dependency probing and backend factory.
     */
    public FancyHologramsAdapterComponent() {
        this(
                plugin -> BukkitDependencyProbe.probe(
                        plugin,
                        "FancyHolograms",
                        MINIMUM_VERSION,
                        null
                ),
                FancyHologramsPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe function.
     * @param portFactory port factory.
     */
    public FancyHologramsAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<HologramPort> portFactory
    ) {
        this.probe = Objects.requireNonNull(probe, "probe");
        this.portFactory = Objects.requireNonNull(portFactory, "portFactory");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEnable(CommonContext context) {
        PortBindingService bindingService = context.services().require(PortBindingService.class);

        BukkitDependencyProbe.ProbeResult probeResult;
        try {
            probeResult = probe.apply(context.plugin());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.HOLOGRAM,
                    "binding-failed:fancyholograms:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.HOLOGRAM, probeResult.reason());
            return;
        }

        try {
            bindingService.bindHologramPort(portFactory.get(), "fancyholograms", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.HOLOGRAM,
                    "binding-failed:fancyholograms:" + ex.getClass().getSimpleName()
            );
        }
    }
}
