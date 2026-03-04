package dev.patric.commonlib.adapter;

import dev.patric.commonlib.adapter.commandapi.CommandApiAdapterComponent;
import dev.patric.commonlib.adapter.fancyholograms.FancyHologramsAdapterComponent;
import dev.patric.commonlib.adapter.fancynpcs.FancyNpcsAdapterComponent;
import dev.patric.commonlib.adapter.fastboard.FastBoardAdapterComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterWave1NoDependencySmokeTest {

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
    void runtimeBootstrapsWithSafeFallbackWhenOptionalDependenciesAreMissing() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .components(java.util.List.of(
                        new CommandApiAdapterComponent(),
                        new FastBoardAdapterComponent(),
                        new FancyHologramsAdapterComponent(),
                        new FancyNpcsAdapterComponent()
                ))
                .build();

        runtime.onLoad();
        runtime.onEnable();

        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.SCOREBOARD));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.HOLOGRAM));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.NPC));

        assertEquals(
                "missing-plugin:CommandAPI",
                capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().reason()
        );
        assertEquals(
                "fastboard:2.1.5",
                capabilityRegistry.status(StandardCapabilities.SCOREBOARD).orElseThrow().metadata()
        );

        runtime.onDisable();
    }
}
