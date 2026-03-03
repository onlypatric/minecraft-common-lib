package dev.patric.commonlib.api.port;

import java.util.concurrent.CompletableFuture;

/**
 * Future arena reset integration port.
 */
public interface ArenaResetPort {

    /**
     * Resets arena identified by key.
     *
     * @param arenaKey arena key.
     * @return completion future.
     */
    CompletableFuture<Void> resetArena(String arenaKey);
}
