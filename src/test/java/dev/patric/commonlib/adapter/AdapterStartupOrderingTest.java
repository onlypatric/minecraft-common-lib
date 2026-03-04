package dev.patric.commonlib.adapter;

import dev.patric.commonlib.adapter.commandapi.CommandApiAdapterComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterStartupOrderingTest {

    private ServerMock server;
    private TestPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void adapterBindsOnEnableAndNotOnLoad() {
        AtomicBoolean registered = new AtomicBoolean(false);

        CommandApiAdapterComponent component = new CommandApiAdapterComponent(
                ignored -> dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe.ProbeResult.available("11.1.0"),
                () -> new CommandPort() {
                    @Override
                    public void register(CommandModel model) {
                        registered.set(true);
                    }

                    @Override
                    public void unregister(String root) {
                        // no-op
                    }

                    @Override
                    public boolean supportsSuggestions() {
                        return true;
                    }
                }
        );

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .component(component)
                .build();

        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);
        CommandPort commandPort = runtime.services().require(CommandPort.class);

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertFalse(commandPort.supportsSuggestions());

        runtime.onLoad();

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertFalse(commandPort.supportsSuggestions());

        runtime.onEnable();

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertEquals("commandapi:11.1.0", capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().metadata());
        assertTrue(commandPort.supportsSuggestions());

        commandPort.register(new NoopCommandModel());
        assertTrue(registered.get());

        runtime.onDisable();
    }

    private static final class NoopCommandModel implements CommandModel {

        @Override
        public String root() {
            return "ping";
        }

        @Override
        public java.util.List<dev.patric.commonlib.api.command.CommandNode> nodes() {
            return java.util.List.of();
        }

        @Override
        public dev.patric.commonlib.api.command.CommandExecution execution() {
            return new dev.patric.commonlib.api.command.CommandExecution() {
                @Override
                public dev.patric.commonlib.api.command.ExecutionMode mode() {
                    return dev.patric.commonlib.api.command.ExecutionMode.SYNC;
                }

                @Override
                public java.util.concurrent.CompletionStage<dev.patric.commonlib.api.command.CommandResult> run(
                        dev.patric.commonlib.api.command.CommandContext context
                ) {
                    return java.util.concurrent.CompletableFuture.completedFuture(
                            dev.patric.commonlib.api.command.CommandResult.success()
                    );
                }
            };
        }

        @Override
        public dev.patric.commonlib.api.command.CommandPermission permission() {
            return dev.patric.commonlib.api.command.CommandPermission.optional();
        }

        @Override
        public dev.patric.commonlib.api.command.CommandMetadata metadata() {
            return new dev.patric.commonlib.api.command.CommandMetadata("ping", "/ping");
        }
    }
}
