package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterFallbackNoopTransparencyTest {

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
    void runtimeFallsBackToNoopPortsWhenAdaptersAreUnavailable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        CommandPort commandPort = runtime.services().require(CommandPort.class);
        ScoreboardPort scoreboardPort = runtime.services().require(ScoreboardPort.class);
        HologramPort hologramPort = runtime.services().require(HologramPort.class);
        NpcPort npcPort = runtime.services().require(NpcPort.class);
        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);

        ScoreboardSession session = new ScoreboardSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main",
                new ScoreboardSnapshot("Title", List.of("L1", "L2")),
                ScoreboardSessionStatus.OPEN,
                System.currentTimeMillis(),
                0L
        );

        assertFalse(commandPort.supportsSuggestions());
        assertTrue(scoreboardPort.open(session));
        assertTrue(scoreboardPort.render(session.sessionId(), session.snapshot()));
        assertTrue(scoreboardPort.close(session.sessionId(), HudAudienceCloseReason.MANUAL));

        UUID hologramId = hologramPort.create("spawn", new Location(null, 0, 64, 0), List.of("Welcome"));
        UUID npcId = npcPort.spawn("merchant", new Location(null, 0, 64, 1), "Trader");

        assertNotNull(hologramId);
        assertNotNull(npcId);
        assertFalse(hologramPort.delete(hologramId));
        assertFalse(npcPort.despawn(npcId));

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.SCOREBOARD));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.HOLOGRAM));
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.NPC));
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.SCOREBOARD).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.HOLOGRAM).orElseThrow().reason());
        assertEquals("No adapter installed", capabilityRegistry.status(StandardCapabilities.NPC).orElseThrow().reason());
    }
}
