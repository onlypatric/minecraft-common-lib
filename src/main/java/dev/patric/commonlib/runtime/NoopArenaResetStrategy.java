package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.arena.ArenaInstance;
import dev.patric.commonlib.api.arena.ArenaResetContext;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.arena.ArenaResetStrategy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * No-op reset strategy used as safe fallback.
 */
public final class NoopArenaResetStrategy implements ArenaResetStrategy {

    /** Shared key for no-op strategy. */
    public static final String KEY = "noop";

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public CompletionStage<ArenaResetResult> reset(ArenaInstance arena, ArenaResetContext context) {
        return CompletableFuture.completedFuture(ArenaResetResult.APPLIED);
    }
}
