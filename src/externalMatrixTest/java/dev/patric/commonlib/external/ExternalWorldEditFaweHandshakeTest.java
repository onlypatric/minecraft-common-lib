package dev.patric.commonlib.external;

import dev.patric.commonlib.adapter.fawe.FaweAdapterComponent;
import dev.patric.commonlib.adapter.worldedit.WorldEditAdapterComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalWorldEditFaweHandshakeTest {

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
    void faweWinsWhenBothSchematicBackendsAreAvailable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .components(java.util.List.of(
                        new WorldEditAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("7.3.0"),
                                dev.patric.commonlib.adapter.worldedit.WorldEditSchematicPort::new
                        ),
                        new FaweAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("2.11.0"),
                                dev.patric.commonlib.adapter.fawe.FaweSchematicPort::new
                        )
                ))
                .build();

        runtime.onLoad();
        runtime.onEnable();

        CapabilityRegistry capabilities = runtime.services().require(CapabilityRegistry.class);
        assertTrue(capabilities.isAvailable(StandardCapabilities.SCHEMATIC));
        assertEquals("fawe:2.11.0", capabilities.status(StandardCapabilities.SCHEMATIC).orElseThrow().metadata());

        runtime.onDisable();
    }
}
