package dev.patric.commonlib.adapter.bossbar.paper;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.BossBarPort;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Optional adapter component that binds Paper bossbar backend.
 */
public final class PaperBossBarAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-bossbar-paper";
    /** Backend version label. */
    public static final String BACKEND_VERSION = "paper-1.21.11";

    private final Supplier<BossBarPort> portFactory;

    /**
     * Creates component with default backend factory.
     */
    public PaperBossBarAdapterComponent() {
        this(PaperBossBarPort::new);
    }

    /**
     * Creates component with injected factory (useful for tests).
     *
     * @param portFactory bossbar port factory.
     */
    public PaperBossBarAdapterComponent(Supplier<BossBarPort> portFactory) {
        this.portFactory = Objects.requireNonNull(portFactory, "portFactory");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEnable(CommonContext context) {
        PortBindingService bindingService = context.services().require(PortBindingService.class);
        try {
            bindingService.bindBossBarPort(portFactory.get(), "paper-bossbar", BACKEND_VERSION);
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.BOSSBAR,
                    "binding-failed:paper-bossbar:" + ex.getClass().getSimpleName()
            );
        }
    }
}
