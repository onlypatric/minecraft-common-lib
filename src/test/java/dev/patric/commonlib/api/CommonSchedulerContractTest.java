package dev.patric.commonlib.api;

import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonSchedulerContractTest {

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
    void schedulerRunsSyncDelayedAndRepeatingTasks() {
        CommonRuntime runtime = CommonRuntime.builder(plugin).build();
        runtime.onLoad();
        runtime.onEnable();

        CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
        AtomicInteger syncRuns = new AtomicInteger();
        AtomicInteger delayedRuns = new AtomicInteger();
        AtomicInteger repeatingRuns = new AtomicInteger();

        scheduler.runSync(syncRuns::incrementAndGet);
        scheduler.runSyncLater(2L, delayedRuns::incrementAndGet);
        TaskHandle repeating = scheduler.runSyncRepeating(0L, 1L, repeatingRuns::incrementAndGet);

        server.getScheduler().performTicks(1L);
        server.getScheduler().performTicks(2L);

        repeating.cancel();
        int beforeMoreTicks = repeatingRuns.get();
        server.getScheduler().performTicks(3L);

        assertEquals(1, syncRuns.get());
        assertEquals(1, delayedRuns.get());
        assertTrue(beforeMoreTicks >= 1);
        assertEquals(beforeMoreTicks, repeatingRuns.get());

        runtime.onDisable();
    }
}
