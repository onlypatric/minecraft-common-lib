package dev.patric.commonlib.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Scheduler abstraction that enforces safe Bukkit thread usage.
 */
public interface CommonScheduler {

    /**
     * Runs task on the main thread.
     *
     * @param task task.
     * @return cancellable handle.
     */
    TaskHandle runSync(Runnable task);

    /**
     * Runs delayed task on the main thread.
     *
     * @param delayTicks delay in ticks.
     * @param task task.
     * @return cancellable handle.
     */
    TaskHandle runSyncLater(long delayTicks, Runnable task);

    /**
     * Runs repeating task on the main thread.
     *
     * @param delayTicks first delay in ticks.
     * @param periodTicks period in ticks.
     * @param task task.
     * @return cancellable handle.
     */
    TaskHandle runSyncRepeating(long delayTicks, long periodTicks, Runnable task);

    /**
     * Runs task asynchronously.
     *
     * @param task task.
     * @return cancellable handle.
     */
    TaskHandle runAsync(Runnable task);

    /**
     * Computes value asynchronously.
     *
     * @param supplier supplier executed async.
     * @param <T> value type.
     * @return future result.
     */
    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    /**
     * Checks whether current thread is server primary thread.
     *
     * @return true if primary thread.
     */
    boolean isPrimaryThread();

    /**
     * Ensures current thread is primary thread.
     *
     * @param operationName operation name for diagnostics.
     */
    void requirePrimaryThread(String operationName);
}
