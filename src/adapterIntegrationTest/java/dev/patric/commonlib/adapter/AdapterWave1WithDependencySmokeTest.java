package dev.patric.commonlib.adapter;

import dev.patric.commonlib.adapter.commandapi.CommandApiAdapterComponent;
import dev.patric.commonlib.adapter.fancyholograms.FancyHologramsAdapterComponent;
import dev.patric.commonlib.adapter.fancynpcs.FancyNpcsAdapterComponent;
import dev.patric.commonlib.adapter.fastboard.FastBoardAdapterComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterWave1WithDependencySmokeTest {

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
    void runtimeBindsAdapterBackendsWhenDependencyProbeIsAvailable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .components(List.of(
                        new CommandApiAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("11.1.0"),
                                AvailableCommandPort::new
                        ),
                        new FastBoardAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("2.1.5"),
                                AvailableScoreboardPort::new
                        ),
                        new FancyHologramsAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("2.9.1"),
                                AvailableHologramPort::new
                        ),
                        new FancyNpcsAdapterComponent(
                                p -> BukkitDependencyProbe.ProbeResult.available("2.9.0"),
                                AvailableNpcPort::new
                        )
                ))
                .build();

        runtime.onLoad();
        runtime.onEnable();

        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.SCOREBOARD));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.HOLOGRAM));
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.NPC));

        assertEquals("commandapi:11.1.0", capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().metadata());
        assertEquals("fastboard:2.1.5", capabilityRegistry.status(StandardCapabilities.SCOREBOARD).orElseThrow().metadata());
        assertEquals(
                "fancyholograms:2.9.1",
                capabilityRegistry.status(StandardCapabilities.HOLOGRAM).orElseThrow().metadata()
        );
        assertEquals("fancynpcs:2.9.0", capabilityRegistry.status(StandardCapabilities.NPC).orElseThrow().metadata());

        runtime.onDisable();
    }

    private static final class AvailableCommandPort implements CommandPort {

        @Override
        public void register(CommandModel model) {
            // no-op
        }

        @Override
        public void unregister(String root) {
            // no-op
        }

        @Override
        public boolean supportsSuggestions() {
            return true;
        }
    }

    private static final class AvailableScoreboardPort implements ScoreboardPort {

        @Override
        public boolean open(ScoreboardSession session) {
            return true;
        }

        @Override
        public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
            return true;
        }

        @Override
        public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
            return true;
        }
    }

    private static final class AvailableHologramPort implements HologramPort {

        @Override
        public UUID create(String hologramKey, Location location, List<String> lines) {
            return UUID.randomUUID();
        }

        @Override
        public boolean updateLines(UUID hologramId, List<String> lines) {
            return true;
        }

        @Override
        public boolean move(UUID hologramId, Location location) {
            return true;
        }

        @Override
        public boolean delete(UUID hologramId) {
            return true;
        }
    }

    private static final class AvailableNpcPort implements NpcPort {

        @Override
        public UUID spawn(String templateKey, Location location, String displayName) {
            return UUID.randomUUID();
        }

        @Override
        public boolean despawn(UUID npcId) {
            return true;
        }

        @Override
        public boolean updateDisplayName(UUID npcId, String displayName) {
            return true;
        }

        @Override
        public boolean teleport(UUID npcId, Location location) {
            return true;
        }
    }
}
