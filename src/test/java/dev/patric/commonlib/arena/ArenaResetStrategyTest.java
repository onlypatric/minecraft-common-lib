package dev.patric.commonlib.arena;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.arena.ArenaOpenRequest;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.arena.ArenaResetStrategy;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.runtime.DefaultArenaService;
import dev.patric.commonlib.runtime.PortBackedArenaResetStrategy;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArenaResetStrategyTest {

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
    void portBackedStrategyDelegatesToPortAndReturnsApplied() {
        AtomicReference<String> lastArenaKey = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();

        ArenaResetPort port = arenaKey -> {
            lastArenaKey.set(arenaKey);
            calls.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        };

        DefaultArenaService service = new DefaultArenaService(scheduler, logger, services, port);
        service.open(new ArenaOpenRequest("arena.port", "template.port", "world", PortBackedArenaResetStrategy.KEY, Map.of()));

        ArenaResetResult result = service.reset("arena.port", "manual")
                .toCompletableFuture()
                .join();

        assertEquals(ArenaResetResult.APPLIED, result);
        assertEquals(1, calls.get());
        assertEquals("template.port", lastArenaKey.get());
        assertEquals("arena.port", service.find("arena.port").orElseThrow().arenaId());
    }

    @Test
    void customStrategyAndThrottlingAreHandledDeterministically() {
        CompletableFuture<ArenaResetResult> pending = new CompletableFuture<>();
        AtomicInteger strategyCalls = new AtomicInteger();

        ArenaResetPort port = arenaKey -> CompletableFuture.completedFuture(null);
        DefaultArenaService service = new DefaultArenaService(scheduler, logger, services, port);

        service.registerStrategy(new ArenaResetStrategy() {
            @Override
            public String key() {
                return "slow";
            }

            @Override
            public CompletionStage<ArenaResetResult> reset(
                    dev.patric.commonlib.api.arena.ArenaInstance arena,
                    dev.patric.commonlib.api.arena.ArenaResetContext context
            ) {
                strategyCalls.incrementAndGet();
                return pending;
            }
        });

        service.open(new ArenaOpenRequest("arena.slow", "template.slow", "world", "slow", Map.of()));

        CompletionStage<ArenaResetResult> first = service.reset("arena.slow", "benchmark");
        ArenaResetResult second = service.reset("arena.slow", "benchmark")
                .toCompletableFuture()
                .join();

        assertEquals(ArenaResetResult.THROTTLED, second);
        assertEquals(1, strategyCalls.get());
        assertEquals(dev.patric.commonlib.api.arena.ArenaStatus.RESETTING,
                service.find("arena.slow").orElseThrow().status());

        pending.complete(ArenaResetResult.APPLIED);
        assertEquals(ArenaResetResult.APPLIED, first.toCompletableFuture().join());
        assertEquals(dev.patric.commonlib.api.arena.ArenaStatus.ACTIVE,
                service.find("arena.slow").orElseThrow().status());

        ArenaResetResult third = service.reset("arena.slow", "benchmark")
                .toCompletableFuture()
                .join();
        assertEquals(ArenaResetResult.APPLIED, third);
        assertTrue(strategyCalls.get() >= 2);
    }
}
