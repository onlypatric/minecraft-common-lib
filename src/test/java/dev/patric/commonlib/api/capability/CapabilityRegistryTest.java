package dev.patric.commonlib.api.capability;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilityRegistryTest {

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
    void registryPublishAndStatusAreTypeSafe() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        registry.publish(StandardCapabilities.NPC, CapabilityStatus.unavailable("No adapter installed"));
        registry.publish(StandardCapabilities.CLAIMS, CapabilityStatus.available("huskclaims"));

        assertFalse(registry.isAvailable(StandardCapabilities.NPC));
        assertTrue(registry.isAvailable(StandardCapabilities.CLAIMS));
        assertEquals("No adapter installed", registry.status(StandardCapabilities.NPC).orElseThrow().reason());
        assertEquals("huskclaims", registry.status(StandardCapabilities.CLAIMS).orElseThrow().metadata());
    }

    @Test
    void runtimeRegistersNoopPortsAndUnavailableCapabilities() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        CapabilityRegistry registry = runtime.services().require(CapabilityRegistry.class);
        assertFalse(registry.isAvailable(StandardCapabilities.NPC));
        assertFalse(registry.isAvailable(StandardCapabilities.HOLOGRAM));
        assertFalse(registry.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(registry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertFalse(registry.isAvailable(StandardCapabilities.GUI));
        assertEquals("No adapter installed", registry.status(StandardCapabilities.NPC).orElseThrow().reason());
        assertEquals("No adapter installed", registry.status(StandardCapabilities.GUI).orElseThrow().reason());

        runtime.services().require(NpcPort.class);
        runtime.services().require(HologramPort.class);
        runtime.services().require(ClaimsPort.class);
        runtime.services().require(SchematicPort.class);
        runtime.services().require(GuiPort.class);
    }
}
