package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterCapabilityTransitionV2Test {

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
    void v2CapabilitiesTransitionBetweenUnavailableAndBoundStates() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        PortBindingService bindingService = runtime.services().require(PortBindingService.class);
        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.BOSSBAR));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.METRICS));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.PACKETS));

        bindingService.bindClaimsPort(new AvailableClaimsPort(), "huskclaims", "4.7.1");
        bindingService.bindSchematicPort(new AvailableSchematicPort(), "fawe", "2.11.0");
        bindingService.bindBossBarPort(new AvailableBossBarPort(), "paper-bossbar", "paper-1.21.11");
        bindingService.bindMetricsPort(new AvailableMetricsPort(), "bstats", "3.1.0");
        bindingService.bindPacketPort(new AvailablePacketPort(), "protocollib", "5.3.0");

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.BOSSBAR));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.METRICS));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.PACKETS));

        assertEquals("fawe:2.11.0", capabilityRegistry.status(StandardCapabilities.SCHEMATIC).orElseThrow().metadata());
        assertEquals("protocollib:5.3.0", capabilityRegistry.status(StandardCapabilities.PACKETS).orElseThrow().metadata());

        bindingService.markUnavailable(StandardCapabilities.PACKETS, "missing-plugin:ProtocolLib");
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.PACKETS));
        assertEquals("missing-plugin:ProtocolLib", capabilityRegistry.status(StandardCapabilities.PACKETS).orElseThrow().reason());

        runtime.onDisable();
    }

    private static final class AvailableClaimsPort implements ClaimsPort {

        @Override
        public boolean isInsideClaim(UUID playerId, Location location) {
            return true;
        }

        @Override
        public Optional<String> claimIdAt(Location location) {
            return Optional.of("claim-main");
        }

        @Override
        public boolean hasBuildPermission(UUID playerId, String claimId) {
            return true;
        }

        @Override
        public boolean hasCombatPermission(UUID playerId, String claimId) {
            return false;
        }
    }

    private static final class AvailableSchematicPort implements SchematicPort {

        @Override
        public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class AvailableBossBarPort implements BossBarPort {

        @Override
        public boolean open(BossBarSession session) {
            return true;
        }

        @Override
        public boolean render(UUID barId, BossBarState state) {
            return true;
        }

        @Override
        public boolean close(UUID barId, HudAudienceCloseReason reason) {
            return true;
        }
    }

    private static final class AvailableMetricsPort implements MetricsPort {

        @Override
        public boolean initialize(org.bukkit.plugin.java.JavaPlugin plugin, int pluginId) {
            return true;
        }

        @Override
        public boolean addSimplePie(String chartId, java.util.function.Supplier<String> supplier) {
            return true;
        }

        @Override
        public boolean addSingleLineChart(String chartId, java.util.function.IntSupplier supplier) {
            return true;
        }

        @Override
        public void shutdown() {
            // no-op
        }
    }

    private static final class AvailablePacketPort implements PacketPort {

        @Override
        public PacketListenerHandle register(PacketListenerOptions options, java.util.function.Consumer<PacketEnvelope> listener) {
            return () -> {
                // no-op
            };
        }

        @Override
        public boolean supportsMutation() {
            return true;
        }

        @Override
        public void unregisterAll() {
            // no-op
        }
    }
}
