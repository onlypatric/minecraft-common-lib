package dev.patric.commonlib.api.arena;

import java.util.concurrent.CompletionStage;

/**
 * Strategy abstraction for arena reset execution.
 */
public interface ArenaResetStrategy {

    /**
     * Returns unique strategy key.
     *
     * @return strategy key.
     */
    String key();

    /**
     * Resets the given arena.
     *
     * @param arena arena instance.
     * @param context reset context.
     * @return completion stage with reset outcome.
     */
    CompletionStage<ArenaResetResult> reset(ArenaInstance arena, ArenaResetContext context);
}
