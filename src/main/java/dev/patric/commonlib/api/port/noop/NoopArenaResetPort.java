package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.port.ArenaResetPort;
import java.util.concurrent.CompletableFuture;

/**
 * No-op arena reset port.
 */
public final class NoopArenaResetPort implements ArenaResetPort {

    @Override
    public CompletableFuture<Void> resetArena(String arenaKey) {
        return CompletableFuture.completedFuture(null);
    }
}
