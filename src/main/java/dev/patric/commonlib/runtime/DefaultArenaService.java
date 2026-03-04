package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.arena.ArenaInstance;
import dev.patric.commonlib.api.arena.ArenaOpenRequest;
import dev.patric.commonlib.api.arena.ArenaResetContext;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.arena.ArenaResetStrategy;
import dev.patric.commonlib.api.arena.ArenaService;
import dev.patric.commonlib.api.arena.ArenaStatus;
import dev.patric.commonlib.api.port.ArenaResetPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default arena service with strategy registry and reset throttling.
 */
public final class DefaultArenaService implements ArenaService {

    private final Map<String, ArenaRecord> arenas;
    private final Map<String, ArenaResetStrategy> strategies;
    private final CommonScheduler scheduler;
    private final RuntimeLogger logger;
    private final ServiceRegistry services;

    /**
     * Creates default arena service.
     *
     * @param scheduler runtime scheduler.
     * @param logger runtime logger.
     * @param services runtime services.
     * @param arenaResetPort arena reset port used by port-backed strategy.
     */
    public DefaultArenaService(
            CommonScheduler scheduler,
            RuntimeLogger logger,
            ServiceRegistry services,
            ArenaResetPort arenaResetPort
    ) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.services = Objects.requireNonNull(services, "services");
        Objects.requireNonNull(arenaResetPort, "arenaResetPort");
        this.arenas = new ConcurrentHashMap<>();
        this.strategies = new ConcurrentHashMap<>();

        registerStrategy(new NoopArenaResetStrategy());
        registerStrategy(new PortBackedArenaResetStrategy(arenaResetPort));
    }

    @Override
    public void registerStrategy(ArenaResetStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy");
        String key = normalizeText(strategy.key(), "strategy.key");
        ArenaResetStrategy previous = strategies.putIfAbsent(key, strategy);
        if (previous != null) {
            throw new IllegalStateException("Arena reset strategy already registered: " + key);
        }
    }

    @Override
    public ArenaInstance open(ArenaOpenRequest request) {
        Objects.requireNonNull(request, "request");

        ArenaRecord record = new ArenaRecord(new ArenaInstance(
                request.arenaId(),
                request.templateKey(),
                request.worldKey(),
                request.metadata(),
                ArenaStatus.ACTIVE
        ), request.resetStrategyKey());

        ArenaRecord previous = arenas.putIfAbsent(record.snapshot.arenaId(), record);
        if (previous != null) {
            throw new IllegalStateException("Arena already exists: " + record.snapshot.arenaId());
        }
        return record.snapshot;
    }

    @Override
    public Optional<ArenaInstance> find(String arenaId) {
        String key = normalizeText(arenaId, "arenaId");
        ArenaRecord record = arenas.get(key);
        if (record == null) {
            return Optional.empty();
        }
        synchronized (record) {
            return Optional.of(record.snapshot);
        }
    }

    @Override
    public List<ArenaInstance> active() {
        List<ArenaInstance> active = new ArrayList<>();
        for (ArenaRecord record : arenas.values()) {
            synchronized (record) {
                if (record.snapshot.status() != ArenaStatus.DISPOSED) {
                    active.add(record.snapshot);
                }
            }
        }
        return List.copyOf(active);
    }

    @Override
    public CompletionStage<ArenaResetResult> reset(String arenaId, String cause) {
        String key = normalizeText(arenaId, "arenaId");
        String normalizedCause = normalizeText(cause, "cause");

        ArenaRecord record = arenas.get(key);
        if (record == null) {
            return CompletableFuture.completedFuture(ArenaResetResult.ARENA_NOT_FOUND);
        }

        ArenaResetStrategy strategy;
        ArenaInstance snapshot;
        synchronized (record) {
            if (record.snapshot.status() == ArenaStatus.DISPOSED) {
                return CompletableFuture.completedFuture(ArenaResetResult.ARENA_DISPOSED);
            }
            if (record.resetInFlight) {
                return CompletableFuture.completedFuture(ArenaResetResult.THROTTLED);
            }

            record.resetInFlight = true;
            record.snapshot = record.snapshotWithStatus(ArenaStatus.RESETTING);
            snapshot = record.snapshot;
            strategy = resolveStrategy(record.strategyKey);
        }

        ArenaResetContext context = new ArenaResetContext(services, normalizedCause, System.currentTimeMillis());

        CompletionStage<ArenaResetResult> stage;
        try {
            stage = strategy.reset(snapshot, context);
        } catch (RuntimeException ex) {
            logger.error("arena reset strategy threw before stage completion: " + strategy.key(), ex);
            stage = CompletableFuture.completedFuture(ArenaResetResult.FAILED);
        }

        return stage.handle((result, throwable) -> {
            ArenaResetResult effective = result == null ? ArenaResetResult.FAILED : result;
            if (throwable != null) {
                logger.error("arena reset failed for " + key, unwrapThrowable(throwable));
                effective = ArenaResetResult.FAILED;
            }

            synchronized (record) {
                record.resetInFlight = false;
                if (effective == ArenaResetResult.ARENA_DISPOSED) {
                    record.snapshot = record.snapshotWithStatus(ArenaStatus.DISPOSED);
                    arenas.remove(key, record);
                } else if (record.snapshot.status() != ArenaStatus.DISPOSED) {
                    record.snapshot = record.snapshotWithStatus(ArenaStatus.ACTIVE);
                }
            }
            return effective;
        });
    }

    @Override
    public boolean dispose(String arenaId) {
        String key = normalizeText(arenaId, "arenaId");
        ArenaRecord record = arenas.remove(key);
        if (record == null) {
            return false;
        }
        synchronized (record) {
            record.resetInFlight = false;
            record.snapshot = record.snapshotWithStatus(ArenaStatus.DISPOSED);
        }
        return true;
    }

    private ArenaResetStrategy resolveStrategy(String strategyKey) {
        ArenaResetStrategy strategy = strategies.get(strategyKey);
        if (strategy != null) {
            return strategy;
        }

        logger.warn("missing arena reset strategy '" + strategyKey + "', falling back to noop");
        return strategies.get(NoopArenaResetStrategy.KEY);
    }

    private static String normalizeText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static Throwable unwrapThrowable(Throwable throwable) {
        if (throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }

    private static final class ArenaRecord {

        private final String strategyKey;

        private ArenaInstance snapshot;
        private boolean resetInFlight;

        private ArenaRecord(ArenaInstance snapshot, String strategyKey) {
            this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
            this.strategyKey = normalizeText(strategyKey, "strategyKey");
            this.resetInFlight = false;
        }

        private ArenaInstance snapshotWithStatus(ArenaStatus status) {
            return new ArenaInstance(
                    snapshot.arenaId(),
                    snapshot.templateKey(),
                    snapshot.worldKey(),
                    snapshot.metadata(),
                    status
            );
        }
    }
}
