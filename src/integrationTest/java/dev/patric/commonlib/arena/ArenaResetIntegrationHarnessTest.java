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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArenaResetIntegrationHarnessTest {

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
    void runtimePathSupportsPortBackedArenaResetPipeline() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
        RuntimeLogger logger = runtime.services().require(RuntimeLogger.class);
        ServiceRegistry services = runtime.services();

        AtomicInteger resetInvocations = new AtomicInteger();
        ArenaResetPort port = arenaKey -> {
            resetInvocations.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        };

        DefaultArenaService service = new DefaultArenaService(scheduler, logger, services, port);
        service.open(new ArenaOpenRequest("arena.int", "template.int", "world", PortBackedArenaResetStrategy.KEY, Map.of()));

        ArenaResetResult result = service.reset("arena.int", "integration").toCompletableFuture().join();

        assertEquals(ArenaResetResult.APPLIED, result);
        assertEquals(1, resetInvocations.get());
    }
}
