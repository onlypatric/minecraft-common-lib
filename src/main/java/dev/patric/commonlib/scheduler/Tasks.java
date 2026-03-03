package dev.patric.commonlib.scheduler;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.TaskHandle;
import java.util.Objects;

/**
 * Lightweight facade over Bukkit scheduler APIs.
 *
 * @deprecated Prefer {@link CommonScheduler} directly.
 */
@Deprecated(since = "0.1.0", forRemoval = false)
public final class Tasks {

    private Tasks() {
        throw new UnsupportedOperationException("Tasks is a utility class and cannot be instantiated.");
    }

    /**
     * Runs a task on the next server tick.
     *
     * @param scheduler scheduler facade.
     * @param runnable task to run.
     * @return scheduled task handle.
     */
    public static TaskHandle runNextTick(CommonScheduler scheduler, Runnable runnable) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(runnable, "runnable");
        return scheduler.runSync(runnable);
    }

    /**
     * Runs a task asynchronously using Bukkit scheduler.
     *
     * @param scheduler scheduler facade.
     * @param runnable task to run.
     * @return scheduled task handle.
     */
    public static TaskHandle runAsync(CommonScheduler scheduler, Runnable runnable) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(runnable, "runnable");
        return scheduler.runAsync(runnable);
    }
}
