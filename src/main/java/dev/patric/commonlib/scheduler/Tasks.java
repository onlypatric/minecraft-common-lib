package dev.patric.commonlib.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Lightweight facade over Bukkit scheduler APIs.
 */
public final class Tasks {

    private Tasks() {
        throw new UnsupportedOperationException("Tasks is a utility class and cannot be instantiated.");
    }

    /**
     * Runs a task on the next server tick.
     *
     * @param plugin owning plugin instance.
     * @param runnable task to run.
     * @return scheduled Bukkit task handle.
     */
    public static BukkitTask runNextTick(Plugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    /**
     * Runs a task asynchronously using Bukkit scheduler.
     *
     * @param plugin owning plugin instance.
     * @param runnable task to run.
     * @return scheduled Bukkit task handle.
     */
    public static BukkitTask runAsync(Plugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
