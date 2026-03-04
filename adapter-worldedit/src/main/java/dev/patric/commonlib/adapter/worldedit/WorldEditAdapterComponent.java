package dev.patric.commonlib.adapter.worldedit;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds WorldEdit-backed schematic port when available.
 */
public final class WorldEditAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-worldedit";
    /** Minimum supported WorldEdit version. */
    public static final String MINIMUM_VERSION = "7.3.0";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<SchematicPort> portFactory;

    /**
     * Creates component with default dependency probe and backend factory.
     */
    public WorldEditAdapterComponent() {
        this(
                plugin -> BukkitDependencyProbe.probe(
                        plugin,
                        "WorldEdit",
                        MINIMUM_VERSION,
                        null
                ),
                WorldEditSchematicPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe.
     * @param portFactory schematic port factory.
     */
    public WorldEditAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<SchematicPort> portFactory
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
                    StandardCapabilities.SCHEMATIC,
                    "binding-failed:worldedit:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.SCHEMATIC, probeResult.reason());
            return;
        }

        try {
            bindingService.bindSchematicPort(portFactory.get(), "worldedit", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.SCHEMATIC,
                    "binding-failed:worldedit:" + ex.getClass().getSimpleName()
            );
        }
    }
}
