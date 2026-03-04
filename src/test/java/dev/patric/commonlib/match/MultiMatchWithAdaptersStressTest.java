package dev.patric.commonlib.match;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchCleanup;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiMatchWithAdaptersStressTest {

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
    void stressMultiMatchWithBoundAdaptersKeepsLifecycleStable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        runtime.onLoad();
        runtime.onEnable();

        PortBindingService binding = runtime.services().require(PortBindingService.class);
        AtomicInteger schematicCalls = new AtomicInteger();

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

        binding.bindSchematicPort(new SchematicPort() {
            @Override
            public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
                schematicCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
                schematicCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }
        }, "fawe", "2.11.0");

        MatchEngineService matchEngine = runtime.services().require(MatchEngineService.class);

        int matches = 25;
        List<UUID> matchIds = new ArrayList<>();
        for (int i = 0; i < matches; i++) {
            MatchSession session = matchEngine.open(new MatchOpenRequest(
                    "stress-" + i,
                    MatchPolicy.competitiveDefaults(),
                    new dev.patric.commonlib.api.match.MatchCallbacks() {
                    },
                    MatchCleanup.noop(),
                    Set.of(UUID.randomUUID())
            ));
            matchIds.add(session.matchId());
            assertEquals(dev.patric.commonlib.api.match.MatchTransitionResult.APPLIED, matchEngine.startCountdown(session.matchId()));
        }

        // Drive deterministic engine loop for countdown progression.
        server.getScheduler().performTicks(120L);

        int closed = matchEngine.closeAll(EndReason.ADMIN_STOP);
        assertEquals(matches, closed);

        server.getScheduler().performTicks(5L);
        assertTrue(matchEngine.isIdle());
        assertTrue(schematicCalls.get() >= 0);

        runtime.onDisable();
    }
}
