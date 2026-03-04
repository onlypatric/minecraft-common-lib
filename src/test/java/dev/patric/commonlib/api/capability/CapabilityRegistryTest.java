package dev.patric.commonlib.api.capability;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.arena.ArenaService;
import dev.patric.commonlib.api.persistence.SchemaMigrationService;
import dev.patric.commonlib.api.persistence.SqlPersistencePort;
import dev.patric.commonlib.api.persistence.YamlPersistencePort;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.team.PartyService;
import dev.patric.commonlib.api.team.TeamService;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilityRegistryTest {

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
    void registryPublishAndStatusAreTypeSafe() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        registry.publish(StandardCapabilities.NPC, CapabilityStatus.unavailable("No adapter installed"));
        registry.publish(StandardCapabilities.CLAIMS, CapabilityStatus.available("huskclaims"));

        assertFalse(registry.isAvailable(StandardCapabilities.NPC));
        assertTrue(registry.isAvailable(StandardCapabilities.CLAIMS));
        assertEquals("No adapter installed", registry.status(StandardCapabilities.NPC).orElseThrow().reason());
        assertEquals("huskclaims", registry.status(StandardCapabilities.CLAIMS).orElseThrow().metadata());
    }

    @Test
    void runtimeRegistersNoopPortsAndUnavailableCapabilities() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        CapabilityRegistry registry = runtime.services().require(CapabilityRegistry.class);
        assertFalse(registry.isAvailable(StandardCapabilities.NPC));
        assertFalse(registry.isAvailable(StandardCapabilities.HOLOGRAM));
        assertFalse(registry.isAvailable(StandardCapabilities.CLAIMS));
        assertFalse(registry.isAvailable(StandardCapabilities.SCHEMATIC));
        assertFalse(registry.isAvailable(StandardCapabilities.GUI));
        assertFalse(registry.isAvailable(StandardCapabilities.COMMAND));
        assertFalse(registry.isAvailable(StandardCapabilities.SCOREBOARD));
        assertFalse(registry.isAvailable(StandardCapabilities.BOSSBAR));
        assertTrue(registry.isAvailable(StandardCapabilities.MATCH_ENGINE));
        assertFalse(registry.isAvailable(StandardCapabilities.ARENA_RESET));
        assertTrue(registry.isAvailable(StandardCapabilities.PERSISTENCE_YAML));
        assertFalse(registry.isAvailable(StandardCapabilities.PERSISTENCE_SQL));
        assertTrue(registry.isAvailable(StandardCapabilities.TEAMS));
        assertTrue(registry.isAvailable(StandardCapabilities.PARTIES));
        assertEquals("No adapter installed", registry.status(StandardCapabilities.NPC).orElseThrow().reason());
        assertEquals("No adapter installed", registry.status(StandardCapabilities.GUI).orElseThrow().reason());
        assertEquals("No adapter installed", registry.status(StandardCapabilities.COMMAND).orElseThrow().reason());
        assertEquals("No adapter installed", registry.status(StandardCapabilities.SCOREBOARD).orElseThrow().reason());
        assertEquals("No adapter installed", registry.status(StandardCapabilities.BOSSBAR).orElseThrow().reason());
        assertEquals("core-default", registry.status(StandardCapabilities.MATCH_ENGINE).orElseThrow().metadata());
        assertEquals("No adapter installed", registry.status(StandardCapabilities.ARENA_RESET).orElseThrow().reason());
        assertEquals("core-default", registry.status(StandardCapabilities.PERSISTENCE_YAML).orElseThrow().metadata());
        assertEquals(
                "No SQL adapter configured",
                registry.status(StandardCapabilities.PERSISTENCE_SQL).orElseThrow().reason()
        );
        assertEquals("core-default", registry.status(StandardCapabilities.TEAMS).orElseThrow().metadata());
        assertEquals("core-default", registry.status(StandardCapabilities.PARTIES).orElseThrow().metadata());

        runtime.services().require(NpcPort.class);
        runtime.services().require(HologramPort.class);
        runtime.services().require(ClaimsPort.class);
        runtime.services().require(SchematicPort.class);
        runtime.services().require(CommandPort.class);
        runtime.services().require(GuiPort.class);
        runtime.services().require(ScoreboardPort.class);
        runtime.services().require(BossBarPort.class);
        runtime.services().require(MatchEngineService.class);
        runtime.services().require(ArenaService.class);
        runtime.services().require(TeamService.class);
        runtime.services().require(PartyService.class);
        runtime.services().require(YamlPersistencePort.class);
        runtime.services().require(SqlPersistencePort.class);
        runtime.services().require(SchemaMigrationService.class);
    }
}
