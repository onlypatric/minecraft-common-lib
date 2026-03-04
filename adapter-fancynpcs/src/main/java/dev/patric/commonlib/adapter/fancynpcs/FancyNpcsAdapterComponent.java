package dev.patric.commonlib.adapter.fancynpcs;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds FancyNpcs-backed npc port when available.
 */
public final class FancyNpcsAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-fancynpcs";
    /** Minimum supported FancyNpcs version. */
    public static final String MINIMUM_VERSION = "2.9.0";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<NpcPort> portFactory;

    /**
     * Creates component with default dependency probing and backend factory.
     */
    public FancyNpcsAdapterComponent() {
        this(
                plugin -> BukkitDependencyProbe.probe(
                        plugin,
                        "FancyNpcs",
                        MINIMUM_VERSION,
                        null
                ),
                FancyNpcsPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe function.
     * @param portFactory port factory.
     */
    public FancyNpcsAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<NpcPort> portFactory
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
                    StandardCapabilities.NPC,
                    "binding-failed:fancynpcs:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.NPC, probeResult.reason());
            return;
        }

        try {
            bindingService.bindNpcPort(portFactory.get(), "fancynpcs", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.NPC,
                    "binding-failed:fancynpcs:" + ex.getClass().getSimpleName()
            );
        }
    }
}
