package dev.patric.commonlib.adapter.commandapi;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds CommandAPI-backed command port when available.
 */
public final class CommandApiAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-commandapi";
    /** Minimum supported CommandAPI version. */
    public static final String MINIMUM_VERSION = "11.1.0";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<CommandPort> portFactory;

    /**
     * Creates component with default dependency probing and backend factory.
     */
    public CommandApiAdapterComponent() {
        this(
                plugin -> BukkitDependencyProbe.probe(
                        plugin,
                        "CommandAPI",
                        MINIMUM_VERSION,
                        "dev.jorel.commandapi.CommandAPI"
                ),
                CommandApiCommandPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe function.
     * @param portFactory port factory.
     */
    public CommandApiAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<CommandPort> portFactory
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
                    StandardCapabilities.COMMAND,
                    "binding-failed:commandapi:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.COMMAND, probeResult.reason());
            return;
        }

        try {
            bindingService.bindCommandPort(portFactory.get(), "commandapi", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.COMMAND,
                    "binding-failed:commandapi:" + ex.getClass().getSimpleName()
            );
        }
    }
}
