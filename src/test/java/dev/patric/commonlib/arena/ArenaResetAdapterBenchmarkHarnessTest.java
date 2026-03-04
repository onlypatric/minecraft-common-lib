package dev.patric.commonlib.arena;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.arena.ArenaOpenRequest;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.runtime.DefaultArenaService;
import dev.patric.commonlib.runtime.PortBackedArenaResetStrategy;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArenaResetAdapterBenchmarkHarnessTest {

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
    void benchmarkScenarioWithBoundSchematicAdapterProducesExpectedCounters() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
        RuntimeLogger logger = runtime.services().require(RuntimeLogger.class);
        ServiceRegistry services = runtime.services();
        PortBindingService binding = services.require(PortBindingService.class);

        AtomicInteger schematicResets = new AtomicInteger();
        binding.bindSchematicPort(new SchematicPort() {
            @Override
            public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
                schematicResets.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }
        }, "fawe", "2.11.0");

        ArenaResetPort port = arenaKey -> services.require(SchematicPort.class)
                .resetRegion(arenaKey, "template", new PasteOptions(false, false, false, 5000));

        DefaultArenaService arenaService = new DefaultArenaService(scheduler, logger, services, port);

        int arenas = 40;
        for (int i = 0; i < arenas; i++) {
            arenaService.open(new ArenaOpenRequest(
                    "arena-bench-" + i,
                    "template-" + i,
                    "world",
                    PortBackedArenaResetStrategy.KEY,
                    Map.of()
            ));
        }

        int applied = 0;
        for (int i = 0; i < arenas; i++) {
            ArenaResetResult result = arenaService.reset("arena-bench-" + i, "benchmark").toCompletableFuture().join();
            if (result == ArenaResetResult.APPLIED) {
                applied++;
            }
        }

        assertEquals(arenas, applied);
        assertEquals(arenas, schematicResets.get());

        runtime.onDisable();
    }
}
