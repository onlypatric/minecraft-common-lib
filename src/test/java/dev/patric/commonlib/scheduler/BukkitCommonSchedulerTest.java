package dev.patric.commonlib.scheduler;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitCommonSchedulerTest {

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
    void repeatingTaskIsCancelledOnRuntimeDisable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        runtime.onLoad();
        runtime.onEnable();

        CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
        AtomicInteger ticks = new AtomicInteger();
        TaskHandle handle = scheduler.runSyncRepeating(0L, 1L, ticks::incrementAndGet);

        server.getScheduler().performTicks(5L);
        int beforeDisable = ticks.get();

        runtime.onDisable();
        server.getScheduler().performTicks(5L);

        assertTrue(handle.isCancelled());
        assertEquals(beforeDisable, ticks.get());
    }

    @Test
    void requirePrimaryThreadDetectsAsyncUsage() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        runtime.onLoad();

        CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
        assertDoesNotThrow(() -> scheduler.requirePrimaryThread("sync-check"));

        CompletableFuture<Boolean> asyncCheck = CompletableFuture.supplyAsync(() -> {
            try {
                scheduler.requirePrimaryThread("async-check");
                return false;
            } catch (IllegalStateException ex) {
                return true;
            }
        });

        assertTrue(asyncCheck.orTimeout(2, TimeUnit.SECONDS).join());
    }
}
