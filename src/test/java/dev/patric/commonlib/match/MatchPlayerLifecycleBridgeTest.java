package dev.patric.commonlib.match;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Set;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchPlayerLifecycleBridgeTest {

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
    void playerQuitAndWorldChangeAreForwardedToMatchEngine() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        runtime.onLoad();
        runtime.onEnable();

        MatchEngineService matchEngine = runtime.services().require(MatchEngineService.class);

        PlayerMock first = server.addPlayer();
        PlayerMock second = server.addPlayer();

        MatchSession firstMatch = matchEngine.open(new MatchOpenRequest(
                "bridge.first",
                MatchPolicy.competitiveDefaults(),
                new MatchCallbacks() {
                },
                (session, reason, services) -> {
                },
                Set.of(first.getUniqueId())
        ));
        MatchSession secondMatch = matchEngine.open(new MatchOpenRequest(
                "bridge.second",
                MatchPolicy.competitiveDefaults(),
                new MatchCallbacks() {
                },
                (session, reason, services) -> {
                },
                Set.of(second.getUniqueId())
        ));

        server.getPluginManager().callEvent(new PlayerQuitEvent(first, "bye"));
        MatchSession firstSnapshot = matchEngine.find(firstMatch.matchId()).orElseThrow();
        assertFalse(firstSnapshot.connectedPlayers().contains(first.getUniqueId()));
        assertTrue(firstSnapshot.disconnectedPlayers().contains(first.getUniqueId()));

        server.getPluginManager().callEvent(new PlayerChangedWorldEvent(second, second.getWorld()));
        MatchSession secondSnapshot = matchEngine.find(secondMatch.matchId()).orElseThrow();
        assertFalse(secondSnapshot.connectedPlayers().contains(second.getUniqueId()));
        assertTrue(secondSnapshot.disconnectedPlayers().contains(second.getUniqueId()));

        runtime.onDisable();
        assertFalse(matchEngine.find(firstMatch.matchId()).isPresent());
        assertFalse(matchEngine.find(secondMatch.matchId()).isPresent());
    }
}
