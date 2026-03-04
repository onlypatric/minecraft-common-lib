package dev.patric.commonlib.adapter;

import dev.patric.commonlib.adapter.huskclaims.HuskClaimsAdapterComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterWave2StartupOrderingTest {

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
    void wave2AdapterBindsOnlyOnEnablePath() {
        AtomicBoolean claimCalled = new AtomicBoolean(false);

        HuskClaimsAdapterComponent component = new HuskClaimsAdapterComponent(
                ignored -> dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe.ProbeResult.available("4.7.1"),
                () -> new ClaimsPort() {
                    @Override
                    public boolean isInsideClaim(UUID playerId, Location location) {
                        claimCalled.set(true);
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
        );

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .component(component)
                .build();

        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);
        ClaimsPort claimsPort = runtime.services().require(ClaimsPort.class);

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));

        runtime.onLoad();
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));

        runtime.onEnable();

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.CLAIMS));
        assertEquals("huskclaims:4.7.1", capabilityRegistry.status(StandardCapabilities.CLAIMS).orElseThrow().metadata());
        assertTrue(claimsPort.isInsideClaim(UUID.randomUUID(), new Location(null, 0, 0, 0)));
        assertTrue(claimCalled.get());

        runtime.onDisable();
    }
}
