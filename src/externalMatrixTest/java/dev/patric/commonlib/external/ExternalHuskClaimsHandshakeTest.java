package dev.patric.commonlib.external;

import dev.patric.commonlib.adapter.huskclaims.HuskClaimsAdapterComponent;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalHuskClaimsHandshakeTest {

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
    void huskClaimsHandshakeHandlesMissingAndAvailablePaths() {
        CommonRuntime missingRuntime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .component(new HuskClaimsAdapterComponent(
                        p -> BukkitDependencyProbe.ProbeResult.unavailable("missing-plugin:HuskClaims"),
                        dev.patric.commonlib.adapter.huskclaims.HuskClaimsClaimsPort::new
                ))
                .build();

        missingRuntime.onLoad();
        missingRuntime.onEnable();

        CapabilityRegistry missingCapabilities = missingRuntime.services().require(CapabilityRegistry.class);
        assertFalse(missingCapabilities.isAvailable(StandardCapabilities.CLAIMS));
        assertEquals("missing-plugin:HuskClaims", missingCapabilities.status(StandardCapabilities.CLAIMS).orElseThrow().reason());

        missingRuntime.onDisable();

        CommonRuntime availableRuntime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .component(new HuskClaimsAdapterComponent(
                        p -> BukkitDependencyProbe.ProbeResult.available("4.7.1"),
                        dev.patric.commonlib.adapter.huskclaims.HuskClaimsClaimsPort::new
                ))
                .build();

        availableRuntime.onLoad();
        availableRuntime.onEnable();

        CapabilityRegistry availableCapabilities = availableRuntime.services().require(CapabilityRegistry.class);
        assertTrue(availableCapabilities.isAvailable(StandardCapabilities.CLAIMS));

        availableRuntime.onDisable();
    }
}
