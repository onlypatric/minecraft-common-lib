package dev.patric.commonlib.adapter.fastboard;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds FastBoard-backed scoreboard port when classpath is compatible.
 */
public final class FastBoardAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-fastboard";
    /** Bundled FastBoard version. */
    public static final String BUNDLED_VERSION = "2.1.5";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<ScoreboardPort> portFactory;

    /**
     * Creates component with default classpath probing and backend factory.
     */
    public FastBoardAdapterComponent() {
        this(
                plugin -> {
                    try {
                        Class.forName("fr.mrmicky.fastboard.FastBoard", false, plugin.getClass().getClassLoader());
                        return BukkitDependencyProbe.ProbeResult.available(BUNDLED_VERSION);
                    } catch (ClassNotFoundException ex) {
                        return BukkitDependencyProbe.ProbeResult.unavailable("missing-class:fr.mrmicky.fastboard.FastBoard");
                    }
                },
                FastBoardScoreboardPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe function.
     * @param portFactory port factory.
     */
    public FastBoardAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<ScoreboardPort> portFactory
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
                    StandardCapabilities.SCOREBOARD,
                    "binding-failed:fastboard:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.SCOREBOARD, probeResult.reason());
            return;
        }

        try {
            bindingService.bindScoreboardPort(portFactory.get(), "fastboard", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.SCOREBOARD,
                    "binding-failed:fastboard:" + ex.getClass().getSimpleName()
            );
        }
    }
}
