package dev.patric.commonlib.adapter.huskclaims;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds HuskClaims-backed claims port when available.
 */
public final class HuskClaimsAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-huskclaims";
    /** Minimum supported HuskClaims version. */
    public static final String MINIMUM_VERSION = "4.7.0";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<ClaimsPort> portFactory;

    /**
     * Creates component with default dependency probe and backend factory.
     */
    public HuskClaimsAdapterComponent() {
        this(
                plugin -> BukkitDependencyProbe.probe(
                        plugin,
                        "HuskClaims",
                        MINIMUM_VERSION,
                        null
                ),
                HuskClaimsClaimsPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe.
     * @param portFactory claims port factory.
     */
    public HuskClaimsAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<ClaimsPort> portFactory
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
                    StandardCapabilities.CLAIMS,
                    "binding-failed:huskclaims:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.CLAIMS, probeResult.reason());
            return;
        }

        try {
            bindingService.bindClaimsPort(portFactory.get(), "huskclaims", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.CLAIMS,
                    "binding-failed:huskclaims:" + ex.getClass().getSimpleName()
            );
        }
    }
}
