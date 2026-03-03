package dev.patric.commonlib.scheduler;

import dev.patric.commonlib.api.TaskHandle;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.scheduler.BukkitTask;

/**
 * Internal task handle backed by BukkitTask.
 */
public final class TrackedBukkitTaskHandle implements TaskHandle {

    private final BukkitTask bukkitTask;
    private final Runnable onCancel;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    TrackedBukkitTaskHandle(BukkitTask bukkitTask, Runnable onCancel) {
        this.bukkitTask = Objects.requireNonNull(bukkitTask, "bukkitTask");
        this.onCancel = Objects.requireNonNull(onCancel, "onCancel");
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            bukkitTask.cancel();
            onCancel.run();
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get() || bukkitTask.isCancelled();
    }

    BukkitTask bukkitTask() {
        return bukkitTask;
    }
}
