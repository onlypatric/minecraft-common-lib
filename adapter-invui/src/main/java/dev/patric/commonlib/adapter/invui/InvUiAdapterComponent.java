package dev.patric.commonlib.adapter.invui;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;

/**
 * Optional adapter component that binds InvUI-backed GUI port when available.
 */
public final class InvUiAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-invui";

    private final Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe;
    private final Supplier<GuiPort> portFactory;
    private final Consumer<JavaPlugin> invUiBootstrap;

    /**
     * Creates component with default classpath probing and backend factory.
     */
    public InvUiAdapterComponent() {
        this(
                plugin -> {
                    try {
                        Class<?> invUiClass = Class.forName(
                                "xyz.xenondevs.invui.InvUI",
                                false,
                                plugin.getClass().getClassLoader()
                        );
                        String version = Optional.ofNullable(invUiClass.getPackage())
                                .map(Package::getImplementationVersion)
                                .orElse("unknown");
                        return BukkitDependencyProbe.ProbeResult.available(version);
                    } catch (ClassNotFoundException ex) {
                        return BukkitDependencyProbe.ProbeResult.unavailable("missing-class:xyz.xenondevs.invui.InvUI");
                    }
                },
                InvUiGuiPort::new,
                plugin -> InvUI.getInstance().setPlugin(plugin)
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe dependency probe function.
     * @param portFactory port factory.
     */
    public InvUiAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<GuiPort> portFactory
    ) {
        this(probe, portFactory, plugin -> {
        });
    }

    /**
     * Creates component with injected probe/factory/bootstrap handlers.
     *
     * @param probe dependency probe function.
     * @param portFactory port factory.
     * @param invUiBootstrap invui bootstrap callback.
     */
    public InvUiAdapterComponent(
            Function<JavaPlugin, BukkitDependencyProbe.ProbeResult> probe,
            Supplier<GuiPort> portFactory,
            Consumer<JavaPlugin> invUiBootstrap
    ) {
        this.probe = Objects.requireNonNull(probe, "probe");
        this.portFactory = Objects.requireNonNull(portFactory, "portFactory");
        this.invUiBootstrap = Objects.requireNonNull(invUiBootstrap, "invUiBootstrap");
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
                    StandardCapabilities.GUI,
                    "binding-failed:invui:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.GUI, probeResult.reason());
            return;
        }

        try {
            invUiBootstrap.accept(context.plugin());
            bindingService.bindGuiPort(portFactory.get(), "invui", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.GUI,
                    "binding-failed:invui:" + ex.getClass().getSimpleName()
            );
        }
    }
}
