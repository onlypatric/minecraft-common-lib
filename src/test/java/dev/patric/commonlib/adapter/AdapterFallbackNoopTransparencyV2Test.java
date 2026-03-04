package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.packet.PacketDirection;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.packet.PacketListenerPriority;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterFallbackNoopTransparencyV2Test {

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
    void v2PortsStaySafeNoopWhenNoAdaptersAreBound() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        ClaimsPort claimsPort = runtime.services().require(ClaimsPort.class);
        SchematicPort schematicPort = runtime.services().require(SchematicPort.class);
        BossBarPort bossBarPort = runtime.services().require(BossBarPort.class);
        MetricsPort metricsPort = runtime.services().require(MetricsPort.class);
        PacketPort packetPort = runtime.services().require(PacketPort.class);
        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);

        assertFalse(claimsPort.isInsideClaim(UUID.randomUUID(), new Location(null, 0, 0, 0)));
        assertTrue(schematicPort.paste("spawn", new Location(null, 0, 0, 0), new PasteOptions(false, false, false, 1000)).isDone());
        assertTrue(bossBarPort.open(new BossBarSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hud",
                new BossBarState("Title", 0.5f, HudBarColor.BLUE, HudBarStyle.SOLID, true),
                false,
                System.currentTimeMillis(),
                0L
        )));
        assertTrue(bossBarPort.close(UUID.randomUUID(), HudAudienceCloseReason.MANUAL));
        assertTrue(metricsPort.addSimplePie("mode", () -> "solo"));
        assertTrue(metricsPort.addSingleLineChart("players", () -> 5));
        assertFalse(packetPort.supportsMutation());
        packetPort.register(
                new PacketListenerOptions(PacketDirection.INBOUND, List.of("PLAY_CHAT"), PacketListenerPriority.NORMAL, false),
                envelope -> {
                    // no-op
                }
        ).close();

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.BOSSBAR));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.METRICS));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.PACKETS));
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.CLAIMS).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.SCHEMATIC).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.BOSSBAR).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.METRICS).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.PACKETS).orElseThrow().reason());
    }
}
