package dev.patric.commonlib.external;

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
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalClaimsNpcBossPacketsMatrixTest {

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
    void matrixOnOffTransitionsStaySafeAcrossWave2Capabilities() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        PortBindingService binding = runtime.services().require(PortBindingService.class);
        CapabilityRegistry capabilities = runtime.services().require(CapabilityRegistry.class);

        assertFalse(capabilities.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(capabilities.isAvailable(StandardCapabilities.BOSSBAR));
        assertFalse(capabilities.isAvailable(StandardCapabilities.PACKETS));

        binding.bindClaimsPort(new ClaimsPort() {
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
        }, "huskclaims", "4.7.1");

        binding.bindBossBarPort(new BossBarPort() {
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
        }, "paper-bossbar", "paper-1.21.11");

        binding.bindPacketPort(new PacketPort() {
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

        assertTrue(capabilities.isAvailable(StandardCapabilities.CLAIMS));
        assertTrue(capabilities.isAvailable(StandardCapabilities.BOSSBAR));
        assertTrue(capabilities.isAvailable(StandardCapabilities.PACKETS));

        binding.markUnavailable(StandardCapabilities.CLAIMS, "missing-plugin:HuskClaims");
        binding.markUnavailable(StandardCapabilities.BOSSBAR, "binding-failed:paper-bossbar:RuntimeException");
        binding.markUnavailable(StandardCapabilities.PACKETS, "missing-plugin:ProtocolLib");

        assertFalse(capabilities.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(capabilities.isAvailable(StandardCapabilities.BOSSBAR));
        assertFalse(capabilities.isAvailable(StandardCapabilities.PACKETS));

        runtime.onDisable();
    }

    @Test
    void matrixKeepsNpcFromWave1CompatibleWithWave2Bindings() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        NpcPort npcPort = runtime.services().require(NpcPort.class);
        UUID id = npcPort.spawn("merchant", new Location(null, 0, 64, 0), "Trader");

        assertFalse(npcPort.despawn(id));

        runtime.onDisable();
    }
}
