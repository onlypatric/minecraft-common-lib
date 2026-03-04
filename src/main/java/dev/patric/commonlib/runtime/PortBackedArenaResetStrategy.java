package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.arena.ArenaInstance;
import dev.patric.commonlib.api.arena.ArenaResetContext;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.arena.ArenaResetStrategy;
import dev.patric.commonlib.api.port.ArenaResetPort;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * Reset strategy delegating to {@link ArenaResetPort}.
 */
public final class PortBackedArenaResetStrategy implements ArenaResetStrategy {

    /** Shared key for port-backed strategy. */
    public static final String KEY = "port-backed";

    private final ArenaResetPort arenaResetPort;

    /**
     * Creates a port-backed reset strategy.
     *
     * @param arenaResetPort arena reset port.
     */
    public PortBackedArenaResetStrategy(ArenaResetPort arenaResetPort) {
        this.arenaResetPort = Objects.requireNonNull(arenaResetPort, "arenaResetPort");
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public CompletionStage<ArenaResetResult> reset(ArenaInstance arena, ArenaResetContext context) {
        return arenaResetPort.resetArena(arena.templateKey())
                .thenApply(ignored -> ArenaResetResult.APPLIED);
    }
}
