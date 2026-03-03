package dev.patric.commonlib.scheduler;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.TaskHandle;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Bukkit-backed scheduler implementation with owned task tracking.
 */
public final class BukkitCommonScheduler implements CommonScheduler {

    private final JavaPlugin plugin;
    private final Set<TrackedBukkitTaskHandle> taskHandles;
    private final Set<CompletableFuture<?>> asyncFutures;

    /**
     * Creates a scheduler bound to a plugin instance.
     *
     * @param plugin owning plugin.
     */
    public BukkitCommonScheduler(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.taskHandles = ConcurrentHashMap.newKeySet();
        this.asyncFutures = ConcurrentHashMap.newKeySet();
    }

    @Override
    public TaskHandle runSync(Runnable task) {
        return track(plugin.getServer().getScheduler().runTask(plugin, task));
    }

    @Override
    public TaskHandle runSyncLater(long delayTicks, Runnable task) {
        return track(plugin.getServer().getScheduler().runTaskLater(plugin, task, Math.max(0L, delayTicks)));
    }

    @Override
    public TaskHandle runSyncRepeating(long delayTicks, long periodTicks, Runnable task) {
        return track(plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                task,
                Math.max(0L, delayTicks),
                Math.max(1L, periodTicks)
        ));
    }

    @Override
    public TaskHandle runAsync(Runnable task) {
        return track(plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");

        CompletableFuture<T> future = new CompletableFuture<>();
        asyncFutures.add(future);

        TaskHandle handle = runAsync(() -> {
            if (future.isCancelled()) {
                return;
            }
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });

        future.whenComplete((ignored, throwable) -> {
            asyncFutures.remove(future);
            if (future.isCancelled()) {
                handle.cancel();
            }
        });

        return future;
    }

    @Override
    public boolean isPrimaryThread() {
        return plugin.getServer().isPrimaryThread();
    }

    @Override
    public void requirePrimaryThread(String operationName) {
        if (!isPrimaryThread()) {
            throw new IllegalStateException("Operation requires primary thread: " + operationName);
        }
    }

    /**
     * Cancels all tracked tasks and async futures.
     */
    public void cancelAll() {
        for (TrackedBukkitTaskHandle handle : Set.copyOf(taskHandles)) {
            handle.cancel();
        }
        taskHandles.clear();

        for (CompletableFuture<?> future : Set.copyOf(asyncFutures)) {
            future.cancel(true);
        }
        asyncFutures.clear();
    }

    private TrackedBukkitTaskHandle track(BukkitTask bukkitTask) {
        TrackedBukkitTaskHandle handle = new TrackedBukkitTaskHandle(bukkitTask, () -> taskHandles.removeIf(existing -> existing == null || existing.bukkitTask().getTaskId() == bukkitTask.getTaskId()));
        taskHandles.add(handle);
        return handle;
    }
}
