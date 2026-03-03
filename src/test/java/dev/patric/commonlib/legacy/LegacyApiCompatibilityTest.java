package dev.patric.commonlib.legacy;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.plugin.PluginLifecycle;
import dev.patric.commonlib.scheduler.Tasks;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class LegacyApiCompatibilityTest {

    @Test
    void pluginLifecycleContractIsStillUsable() {
        AtomicInteger lifecycleCalls = new AtomicInteger();
        PluginLifecycle lifecycle = new PluginLifecycle() {
            @Override
            public void onEnable() {
                lifecycleCalls.incrementAndGet();
            }

            @Override
            public void onDisable() {
                lifecycleCalls.incrementAndGet();
            }
        };

        lifecycle.onEnable();
        lifecycle.onDisable();

        assertEquals(2, lifecycleCalls.get());
    }

    @Test
    void tasksUtilityStillDelegatesToCommonScheduler() {
        CommonScheduler scheduler = mock(CommonScheduler.class);
        TaskHandle syncHandle = mock(TaskHandle.class);
        TaskHandle asyncHandle = mock(TaskHandle.class);
        Runnable syncTask = () -> { };
        Runnable asyncTask = () -> { };

        when(scheduler.runSync(syncTask)).thenReturn(syncHandle);
        when(scheduler.runAsync(asyncTask)).thenReturn(asyncHandle);

        assertSame(syncHandle, Tasks.runNextTick(scheduler, syncTask));
        assertSame(asyncHandle, Tasks.runAsync(scheduler, asyncTask));

        verify(scheduler).runSync(syncTask);
        verify(scheduler).runAsync(asyncTask);
    }
}
