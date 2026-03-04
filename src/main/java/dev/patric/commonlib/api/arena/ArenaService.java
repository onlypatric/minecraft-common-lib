package dev.patric.commonlib.api.arena;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * High-level service for arena lifecycle and reset strategies.
 */
public interface ArenaService {

    /**
     * Registers reset strategy.
     *
     * @param strategy strategy implementation.
     */
    void registerStrategy(ArenaResetStrategy strategy);

    /**
     * Opens arena instance.
     *
     * @param request open request.
     * @return opened arena snapshot.
     */
    ArenaInstance open(ArenaOpenRequest request);

    /**
     * Finds arena by id.
     *
     * @param arenaId arena identifier.
     * @return arena snapshot when present.
     */
    Optional<ArenaInstance> find(String arenaId);

    /**
     * Lists active arenas.
     *
     * @return immutable list of active arenas.
     */
    List<ArenaInstance> active();

    /**
     * Requests arena reset.
     *
     * @param arenaId arena identifier.
     * @param cause reset cause.
     * @return completion stage with reset outcome.
     */
    CompletionStage<ArenaResetResult> reset(String arenaId, String cause);

    /**
     * Disposes arena instance.
     *
     * @param arenaId arena identifier.
     * @return true when disposed.
     */
    boolean dispose(String arenaId);
}
