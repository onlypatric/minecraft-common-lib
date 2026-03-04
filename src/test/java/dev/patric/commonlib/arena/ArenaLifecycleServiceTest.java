package dev.patric.commonlib.arena;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.arena.ArenaOpenRequest;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.arena.ArenaResetStrategy;
import dev.patric.commonlib.api.arena.ArenaStatus;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.runtime.DefaultArenaService;
import dev.patric.commonlib.runtime.NoopArenaResetStrategy;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArenaLifecycleServiceTest {

    private ServerMock server;
    private TestPlugin plugin;
    private CommonScheduler scheduler;
    private RuntimeLogger logger;
    private ServiceRegistry services;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        scheduler = runtime.services().require(CommonScheduler.class);
        logger = runtime.services().require(RuntimeLogger.class);
        services = runtime.services();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void openFindResetDisposeAndMissingArenaPaths() {
        ArenaResetPort port = arenaKey -> CompletableFuture.completedFuture(null);
        DefaultArenaService service = new DefaultArenaService(scheduler, logger, services, port);

        service.open(new ArenaOpenRequest("arena.lifecycle", "template", "world", NoopArenaResetStrategy.KEY, Map.of()));

        assertEquals(ArenaStatus.ACTIVE, service.find("arena.lifecycle").orElseThrow().status());
        assertEquals(1, service.active().size());

        ArenaResetResult reset = service.reset("arena.lifecycle", "rotation")
                .toCompletableFuture()
                .join();
        assertEquals(ArenaResetResult.APPLIED, reset);
        assertEquals(ArenaStatus.ACTIVE, service.find("arena.lifecycle").orElseThrow().status());

        assertTrue(service.dispose("arena.lifecycle"));
        assertFalse(service.find("arena.lifecycle").isPresent());
        assertTrue(service.active().isEmpty());

        ArenaResetResult missing = service.reset("arena.lifecycle", "rotation")
                .toCompletableFuture()
                .join();
        assertEquals(ArenaResetResult.ARENA_NOT_FOUND, missing);
    }

    @Test
    void inFlightResetTransitionsToResettingAndThrottlesSecondReset() {
        CompletableFuture<ArenaResetResult> pending = new CompletableFuture<>();
        ArenaResetPort port = arenaKey -> CompletableFuture.completedFuture(null);
        DefaultArenaService service = new DefaultArenaService(scheduler, logger, services, port);

        service.registerStrategy(new ArenaResetStrategy() {
            @Override
            public String key() {
                return "pending";
            }

            @Override
            public CompletionStage<ArenaResetResult> reset(
                    dev.patric.commonlib.api.arena.ArenaInstance arena,
                    dev.patric.commonlib.api.arena.ArenaResetContext context
            ) {
                return pending;
            }
        });

        service.open(new ArenaOpenRequest("arena.pending", "template", "world", "pending", Map.of()));

        CompletionStage<ArenaResetResult> first = service.reset("arena.pending", "manual");
        assertEquals(ArenaStatus.RESETTING, service.find("arena.pending").orElseThrow().status());

        ArenaResetResult second = service.reset("arena.pending", "manual")
                .toCompletableFuture()
                .join();
        assertEquals(ArenaResetResult.THROTTLED, second);

        pending.complete(ArenaResetResult.APPLIED);
        assertEquals(ArenaResetResult.APPLIED, first.toCompletableFuture().join());
        assertEquals(ArenaStatus.ACTIVE, service.find("arena.pending").orElseThrow().status());
    }
}
