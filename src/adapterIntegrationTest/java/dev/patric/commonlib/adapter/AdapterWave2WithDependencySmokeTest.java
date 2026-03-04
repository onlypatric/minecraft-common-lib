package dev.patric.commonlib.adapter;

import dev.patric.commonlib.adapter.bossbar.paper.PaperBossBarAdapterComponent;
import dev.patric.commonlib.adapter.bstats.BStatsAdapterComponent;
import dev.patric.commonlib.adapter.fawe.FaweAdapterComponent;
import dev.patric.commonlib.adapter.huskclaims.HuskClaimsAdapterComponent;
import dev.patric.commonlib.adapter.protocollib.ProtocolLibAdapterComponent;
import dev.patric.commonlib.adapter.worldedit.WorldEditAdapterComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterWave2WithDependencySmokeTest {

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
    void runtimeBindsWave2BackendsWhenProbesAreAvailable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .components(List.of(
                        new HuskClaimsAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("4.7.1"),
                                AvailableClaimsPort::new
                        ),
                        new WorldEditAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("7.3.0"),
                                AvailableWorldEditPort::new
                        ),
                        new FaweAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("2.11.0"),
                                AvailableFawePort::new
                        ),
                        new PaperBossBarAdapterComponent(AvailableBossBarPort::new),
                        new BStatsAdapterComponent(12345, AvailableMetricsPort::new),
                        new ProtocolLibAdapterComponent(
                                p -> ProtocolLibAdapterComponent.ProbeResult.available("5.3.0-SNAPSHOT"),
                                AvailablePacketPort::new
                        )
                ))
                .build();

        runtime.onLoad();
        runtime.onEnable();

        CapabilityRegistry capabilities = runtime.services().require(CapabilityRegistry.class);

        assertTrue(capabilities.isAvailable(StandardCapabilities.CLAIMS));
        assertTrue(capabilities.isAvailable(StandardCapabilities.SCHEMATIC));
        assertTrue(capabilities.isAvailable(StandardCapabilities.BOSSBAR));
        assertTrue(capabilities.isAvailable(StandardCapabilities.METRICS));
        assertTrue(capabilities.isAvailable(StandardCapabilities.PACKETS));

        // FAWE must win over WorldEdit when both adapters are available.
        assertEquals("fawe:2.11.0", capabilities.status(StandardCapabilities.SCHEMATIC).orElseThrow().metadata());
        assertEquals("protocollib:5.3.0-SNAPSHOT", capabilities.status(StandardCapabilities.PACKETS).orElseThrow().metadata());

        runtime.onDisable();
    }

    private static final class AvailableClaimsPort implements ClaimsPort {

        @Override
        public boolean isInsideClaim(UUID playerId, Location location) {
            return true;
        }

        @Override
        public Optional<String> claimIdAt(Location location) {
            return Optional.of("claim");
        }

        @Override
        public boolean hasBuildPermission(UUID playerId, String claimId) {
            return true;
        }

        @Override
        public boolean hasCombatPermission(UUID playerId, String claimId) {
            return true;
        }
    }

    private static final class AvailableWorldEditPort implements SchematicPort {

        @Override
        public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class AvailableFawePort implements SchematicPort {

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
        public boolean open(dev.patric.commonlib.api.hud.BossBarSession session) {
            return true;
        }

        @Override
        public boolean render(UUID barId, dev.patric.commonlib.api.hud.BossBarState state) {
            return true;
        }

        @Override
        public boolean close(UUID barId, dev.patric.commonlib.api.hud.HudAudienceCloseReason reason) {
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
