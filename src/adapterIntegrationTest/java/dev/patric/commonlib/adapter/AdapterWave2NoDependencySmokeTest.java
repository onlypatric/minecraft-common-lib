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
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterWave2NoDependencySmokeTest {

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
    void runtimeBootstrapsWithWave2FallbacksWhenDependenciesAreMissing() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .components(List.of(
                        new HuskClaimsAdapterComponent(),
                        new WorldEditAdapterComponent(),
                        new FaweAdapterComponent(),
                        new PaperBossBarAdapterComponent(),
                        new BStatsAdapterComponent(0),
                        new ProtocolLibAdapterComponent()
                ))
                .build();

        runtime.onLoad();
        runtime.onEnable();

        CapabilityRegistry capabilities = runtime.services().require(CapabilityRegistry.class);

        assertFalse(capabilities.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(capabilities.isAvailable(StandardCapabilities.SCHEMATIC));
        assertFalse(capabilities.isAvailable(StandardCapabilities.METRICS));
        assertFalse(capabilities.isAvailable(StandardCapabilities.PACKETS));
        assertTrue(capabilities.isAvailable(StandardCapabilities.BOSSBAR));

        assertEquals("missing-plugin:HuskClaims", capabilities.status(StandardCapabilities.CLAIMS).orElseThrow().reason());
        assertEquals("missing-plugin:FastAsyncWorldEdit", capabilities.status(StandardCapabilities.SCHEMATIC).orElseThrow().reason());
        assertEquals("binding-failed:bstats:invalid-plugin-id", capabilities.status(StandardCapabilities.METRICS).orElseThrow().reason());

        runtime.onDisable();
    }
}
