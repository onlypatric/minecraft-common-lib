package dev.patric.commonlib.arena;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.arena.ArenaOpenRequest;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.runtime.DefaultArenaService;
import dev.patric.commonlib.runtime.PortBackedArenaResetStrategy;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArenaResetBenchmarkHarnessTest {

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
    void benchmarkHarnessProducesDeterministicReportShape() {
        AtomicInteger portCalls = new AtomicInteger();
        ArenaResetPort port = arenaKey -> {
            portCalls.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        };

        DefaultArenaService service = new DefaultArenaService(scheduler, logger, services, port);

        int arenas = 100;
        for (int i = 0; i < arenas; i++) {
            service.open(new ArenaOpenRequest(
                    "arena." + i,
                    "template." + i,
                    "world",
                    PortBackedArenaResetStrategy.KEY,
                    Map.of("slot", String.valueOf(i))
            ));
        }

        List<ArenaResetResult> results = new ArrayList<>();
        for (int i = 0; i < arenas; i++) {
            results.add(service.reset("arena." + i, "benchmark").toCompletableFuture().join());
        }

        long applied = results.stream().filter(result -> result == ArenaResetResult.APPLIED).count();
        long throttled = results.stream().filter(result -> result == ArenaResetResult.THROTTLED).count();
        long failed = results.stream().filter(result -> result == ArenaResetResult.FAILED).count();

        BenchmarkReport report = new BenchmarkReport(arenas, results.size(), (int) applied, (int) throttled, (int) failed);

        assertEquals(arenas, report.arenas());
        assertEquals(arenas, report.resetRequests());
        assertEquals(arenas, report.applied());
        assertEquals(0, report.throttled());
        assertEquals(0, report.failed());
        assertEquals(arenas, portCalls.get());
        assertTrue(report.resetRequests() >= report.applied());
    }

    private record BenchmarkReport(int arenas, int resetRequests, int applied, int throttled, int failed) {
    }
}
