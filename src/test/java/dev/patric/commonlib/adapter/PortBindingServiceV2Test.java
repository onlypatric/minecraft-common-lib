package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.packet.PacketDirection;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.packet.PacketListenerPriority;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.noop.NoopBossBarPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopGuiPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopMetricsPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopPacketPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.noop.NoopScoreboardPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.runtime.adapter.DefaultPortBindingService;
import dev.patric.commonlib.runtime.adapter.DelegatingBossBarPort;
import dev.patric.commonlib.runtime.adapter.DelegatingClaimsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingCommandPort;
import dev.patric.commonlib.runtime.adapter.DelegatingHologramPort;
import dev.patric.commonlib.runtime.adapter.DelegatingGuiPort;
import dev.patric.commonlib.runtime.adapter.DelegatingMetricsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingNpcPort;
import dev.patric.commonlib.runtime.adapter.DelegatingPacketPort;
import dev.patric.commonlib.runtime.adapter.DelegatingSchematicPort;
import dev.patric.commonlib.runtime.adapter.DelegatingScoreboardPort;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortBindingServiceV2Test {

    @Test
    void v2BindingsUpdateCapabilitiesAndFallbackDeterministically() {
        DelegatingClaimsPort claimsPort = new DelegatingClaimsPort(new NoopClaimsPort());
        DelegatingGuiPort guiPort = new DelegatingGuiPort(new NoopGuiPort());
        DelegatingSchematicPort schematicPort = new DelegatingSchematicPort(new NoopSchematicPort());
        DelegatingBossBarPort bossBarPort = new DelegatingBossBarPort(new NoopBossBarPort());
        DelegatingMetricsPort metricsPort = new DelegatingMetricsPort(new NoopMetricsPort());
        DelegatingPacketPort packetPort = new DelegatingPacketPort(new NoopPacketPort());

        CapabilityRegistry capabilityRegistry = new DefaultCapabilityRegistry();
        DefaultPortBindingService bindingService = new DefaultPortBindingService(
                new DelegatingCommandPort(new NoopCommandPort()),
                new DelegatingScoreboardPort(new NoopScoreboardPort()),
                new DelegatingHologramPort(new NoopHologramPort()),
                new DelegatingNpcPort(new NoopNpcPort()),
                guiPort,
                claimsPort,
                schematicPort,
                bossBarPort,
                metricsPort,
                packetPort,
                capabilityRegistry
        );

        bindingService.bindClaimsPort(new ClaimsPort() {
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
        }, "huskclaims", "4.7.1");

        assertTrue(claimsPort.isInsideClaim(UUID.randomUUID(), new Location(null, 0, 0, 0)));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));
        assertEquals("huskclaims:4.7.1", capabilityRegistry.status(StandardCapabilities.CLAIMS).orElseThrow().metadata());

        bindingService.bindGuiPort(new GuiPort() {
            @Override
            public boolean open(dev.patric.commonlib.api.gui.render.GuiRenderModel renderModel) {
                return false;
            }

            @Override
            public boolean render(UUID sessionId, dev.patric.commonlib.api.gui.render.GuiRenderPatch patch) {
                return false;
            }

            @Override
            public boolean close(UUID sessionId, dev.patric.commonlib.api.gui.GuiCloseReason reason) {
                return false;
            }

            @Override
            public boolean supports(dev.patric.commonlib.api.gui.GuiPortFeature feature) {
                return true;
            }
        }, "invui", "unknown");
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.GUI));

        bindingService.bindSchematicPort(new SchematicPort() {
            @Override
            public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
                return CompletableFuture.failedFuture(new IllegalStateException("boom"));
            }

            @Override
            public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
                return CompletableFuture.failedFuture(new IllegalStateException("boom"));
            }
        }, "worldedit", "7.3.0");

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertEquals("worldedit:7.3.0", capabilityRegistry.status(StandardCapabilities.SCHEMATIC).orElseThrow().metadata());

        bindingService.bindBossBarPort(new BossBarPort() {
            @Override
            public boolean open(BossBarSession session) {
                return false;
            }

            @Override
            public boolean render(UUID barId, BossBarState state) {
                return false;
            }

            @Override
            public boolean close(UUID barId, HudAudienceCloseReason reason) {
                return false;
            }
        }, "paper-bossbar", "paper-1.21.11");

        assertFalse(bossBarPort.open(new BossBarSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hud",
                new BossBarState("B", 1.0f, HudBarColor.BLUE, HudBarStyle.SOLID, true),
                false,
                System.currentTimeMillis(),
                0L
        )));

        MetricsPort customMetrics = new MetricsPort() {
            @Override
            public boolean initialize(org.bukkit.plugin.java.JavaPlugin plugin, int pluginId) {
                return true;
            }

            @Override
            public boolean addSimplePie(String chartId, java.util.function.Supplier<String> supplier) {
                return false;
            }

            @Override
            public boolean addSingleLineChart(String chartId, java.util.function.IntSupplier supplier) {
                return false;
            }

            @Override
            public void shutdown() {
                // no-op
            }
        };

        bindingService.bindMetricsPort(customMetrics, "bstats", "3.1.0");
        assertFalse(metricsPort.addSimplePie("mode", () -> "solo"));
        assertEquals("bstats:3.1.0", capabilityRegistry.status(StandardCapabilities.METRICS).orElseThrow().metadata());

        bindingService.bindPacketPort(new PacketPort() {
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
        }, "protocollib", "5.3.0");

        assertTrue(packetPort.supportsMutation());
        assertEquals("protocollib:5.3.0", capabilityRegistry.status(StandardCapabilities.PACKETS).orElseThrow().metadata());

        packetPort.register(
                new PacketListenerOptions(PacketDirection.OUTBOUND, List.of("ENTITY_METADATA"), PacketListenerPriority.NORMAL, true),
                envelope -> {
                    // no-op
                }
        ).close();

        bindingService.markUnavailable(StandardCapabilities.CLAIMS, "missing-plugin:HuskClaims");
        bindingService.markUnavailable(StandardCapabilities.SCHEMATIC, "missing-plugin:WorldEdit");
        bindingService.markUnavailable(StandardCapabilities.BOSSBAR, "binding-failed:paper-bossbar:RuntimeException");
        bindingService.markUnavailable(StandardCapabilities.METRICS, "binding-failed:bstats:initialize");
        bindingService.markUnavailable(StandardCapabilities.PACKETS, "missing-plugin:ProtocolLib");

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.BOSSBAR));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.METRICS));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.PACKETS));

        assertFalse(claimsPort.isInsideClaim(UUID.randomUUID(), new Location(null, 0, 0, 0)));
        assertTrue(schematicPort.paste("demo", new Location(null, 0, 0, 0), new PasteOptions(false, false, false, 1000)).isDone());
        assertTrue(bossBarPort.close(UUID.randomUUID(), HudAudienceCloseReason.MANUAL));
        assertTrue(metricsPort.addSimplePie("mode", () -> "fallback"));
        assertFalse(packetPort.supportsMutation());
    }
}
