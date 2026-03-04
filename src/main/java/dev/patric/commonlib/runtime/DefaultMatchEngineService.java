package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.match.DisconnectResult;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.JoinResult;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchCleanup;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.api.match.MatchSessionStatus;
import dev.patric.commonlib.api.match.MatchState;
import dev.patric.commonlib.api.match.MatchTransitionResult;
import dev.patric.commonlib.api.match.RejoinResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Deterministic, reusable match/state engine.
 */
public final class DefaultMatchEngineService implements MatchEngineService {

    private final Map<UUID, MatchRecord> matches;
    private final CommonScheduler scheduler;
    private final RuntimeLogger logger;
    private final ServiceRegistry services;
    private final AtomicLong tickCounter;
    private final AtomicLong sequence;
    private final Object loopLock;

    private volatile TaskHandle loopHandle;

    /**
     * Creates the default match engine service.
     *
     * @param scheduler scheduler facade.
     * @param logger runtime logger.
     * @param services runtime service registry.
     */
    public DefaultMatchEngineService(CommonScheduler scheduler, RuntimeLogger logger, ServiceRegistry services) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.services = Objects.requireNonNull(services, "services");
        this.matches = new ConcurrentHashMap<>();
        this.tickCounter = new AtomicLong(0L);
        this.sequence = new AtomicLong(0L);
        this.loopLock = new Object();
    }

    @Override
    public MatchSession open(MatchOpenRequest request) {
        Objects.requireNonNull(request, "request");

        long now = System.currentTimeMillis();
        long tick = tickCounter.get();
        MatchRecord record = new MatchRecord(
                UUID.randomUUID(),
                sequence.incrementAndGet(),
                request,
                now,
                tick
        );

        matches.put(record.matchId(), record);
        ensureLoopStarted();

        synchronized (record) {
            if (!invokeStateEnter(record)) {
                return record.snapshot();
            }
            return record.snapshot();
        }
    }

    @Override
    public Optional<MatchSession> find(UUID matchId) {
        Objects.requireNonNull(matchId, "matchId");
        MatchRecord record = matches.get(matchId);
        if (record == null) {
            return Optional.empty();
        }

        synchronized (record) {
            return Optional.of(record.snapshot());
        }
    }

    @Override
    public List<MatchSession> active() {
        List<MatchSession> snapshots = new ArrayList<>();

        for (MatchRecord record : orderedRecords()) {
            synchronized (record) {
                if (record.status == MatchSessionStatus.ACTIVE) {
                    snapshots.add(record.snapshot());
                }
            }
        }

        return List.copyOf(snapshots);
    }

    @Override
    public MatchTransitionResult startCountdown(UUID matchId) {
        Objects.requireNonNull(matchId, "matchId");
        return transition(matchId, MatchState.COUNTDOWN, null);
    }

    @Override
    public MatchTransitionResult transition(UUID matchId, MatchState target, EndReason reason) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(target, "target");

        MatchRecord record = matches.get(matchId);
        if (record == null) {
            return MatchTransitionResult.MATCH_NOT_FOUND;
        }

        synchronized (record) {
            if (record.status == MatchSessionStatus.CLOSED) {
                return MatchTransitionResult.MATCH_CLOSED;
            }

            if (!isValidTransition(record.state, target)) {
                return MatchTransitionResult.INVALID_TRANSITION;
            }

            EndReason effectiveReason = target == MatchState.ENDING
                    ? (reason == null ? EndReason.ADMIN_STOP : reason)
                    : reason;

            return transitionLocked(record, target, effectiveReason);
        }
    }

    @Override
    public MatchTransitionResult end(UUID matchId, EndReason reason) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(reason, "reason");

        MatchRecord record = matches.get(matchId);
        if (record == null) {
            return MatchTransitionResult.MATCH_NOT_FOUND;
        }

        synchronized (record) {
            if (record.status == MatchSessionStatus.CLOSED) {
                return MatchTransitionResult.MATCH_CLOSED;
            }

            forceEndLocked(record, reason);
            return MatchTransitionResult.APPLIED;
        }
    }

    @Override
    public JoinResult join(UUID matchId, UUID playerId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");

        MatchRecord record = matches.get(matchId);
        if (record == null) {
            return JoinResult.MATCH_NOT_FOUND;
        }

        synchronized (record) {
            if (record.status == MatchSessionStatus.CLOSED) {
                return JoinResult.MATCH_CLOSED;
            }

            if (record.connectedPlayers.contains(playerId)) {
                return JoinResult.ALREADY_JOINED;
            }

            record.disconnectedSinceTick.remove(playerId);
            record.connectedPlayers.add(playerId);
            return JoinResult.JOINED;
        }
    }

    @Override
    public DisconnectResult disconnect(UUID matchId, UUID playerId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");

        MatchRecord record = matches.get(matchId);
        if (record == null) {
            return DisconnectResult.MATCH_NOT_FOUND;
        }

        synchronized (record) {
            return disconnectLocked(record, playerId);
        }
    }

    @Override
    public RejoinResult rejoin(UUID matchId, UUID playerId) {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(playerId, "playerId");

        MatchRecord record = matches.get(matchId);
        if (record == null) {
            return RejoinResult.MATCH_NOT_FOUND;
        }

        synchronized (record) {
            if (record.status == MatchSessionStatus.CLOSED) {
                return RejoinResult.MATCH_CLOSED;
            }

            if (record.connectedPlayers.contains(playerId)) {
                return RejoinResult.REJOINED;
            }

            Long disconnectedAtTick = record.disconnectedSinceTick.get(playerId);
            if (disconnectedAtTick == null) {
                return RejoinResult.NOT_PARTICIPANT;
            }

            if (!record.policy.rejoin().enabled()) {
                return RejoinResult.DENIED_POLICY;
            }

            if (!isRejoinStateAllowed(record.state)) {
                return RejoinResult.DENIED_STATE;
            }

            long age = Math.max(0L, record.currentTick - disconnectedAtTick);
            long sessionTimeoutTicks = record.policy.rejoin().sessionTimeoutTicks();
            if (sessionTimeoutTicks > 0L && age > sessionTimeoutTicks) {
                record.disconnectedSinceTick.remove(playerId);
                return RejoinResult.SESSION_EXPIRED;
            }

            long rejoinWindowTicks = record.policy.rejoin().rejoinWindowTicks();
            if (age > rejoinWindowTicks) {
                return RejoinResult.WINDOW_EXPIRED;
            }

            record.disconnectedSinceTick.remove(playerId);
            record.connectedPlayers.add(playerId);
            return RejoinResult.REJOINED;
        }
    }

    @Override
    public int closeAll(EndReason reason) {
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (MatchRecord record : List.copyOf(matches.values())) {
            synchronized (record) {
                if (closeLocked(record, reason)) {
                    closed++;
                }
            }
        }

        stopLoopIfIdle();
        return closed;
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        markPlayerDisconnectedAcrossMatches(playerId);
    }

    @Override
    public void onPlayerWorldChange(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        markPlayerDisconnectedAcrossMatches(playerId);
    }

    @Override
    public boolean isIdle() {
        TaskHandle handle = loopHandle;
        boolean loopStopped = handle == null || handle.isCancelled();
        return matches.isEmpty() && loopStopped;
    }

    private void markPlayerDisconnectedAcrossMatches(UUID playerId) {
        for (MatchRecord record : List.copyOf(matches.values())) {
            synchronized (record) {
                disconnectLocked(record, playerId);
            }
        }
    }

    private DisconnectResult disconnectLocked(MatchRecord record, UUID playerId) {
        if (record.status == MatchSessionStatus.CLOSED) {
            return DisconnectResult.MATCH_CLOSED;
        }

        if (record.connectedPlayers.remove(playerId)) {
            record.disconnectedSinceTick.put(playerId, record.currentTick);
            return DisconnectResult.MARKED_DISCONNECTED;
        }

        if (record.disconnectedSinceTick.containsKey(playerId)) {
            return DisconnectResult.MARKED_DISCONNECTED;
        }

        return DisconnectResult.NOT_PARTICIPANT;
    }

    private void ensureLoopStarted() {
        TaskHandle existing = loopHandle;
        if (existing != null && !existing.isCancelled()) {
            return;
        }

        synchronized (loopLock) {
            existing = loopHandle;
            if (existing != null && !existing.isCancelled()) {
                return;
            }
            loopHandle = scheduler.runSyncRepeating(1L, 1L, this::runEngineTick);
        }
    }

    private void stopLoopIfIdle() {
        if (!matches.isEmpty()) {
            return;
        }

        synchronized (loopLock) {
            if (!matches.isEmpty()) {
                return;
            }

            TaskHandle handle = loopHandle;
            if (handle != null) {
                handle.cancel();
                loopHandle = null;
            }
        }
    }

    private void runEngineTick() {
        long tick = tickCounter.incrementAndGet();

        for (MatchRecord record : orderedRecords()) {
            synchronized (record) {
                if (record.status == MatchSessionStatus.CLOSED) {
                    continue;
                }

                record.currentTick = tick;
                expireDisconnectedSessionsLocked(record, tick);

                long stateTick = Math.max(0L, tick - record.stateEnteredTick);
                if (!invokeStateTick(record, stateTick)) {
                    continue;
                }

                if (record.status == MatchSessionStatus.CLOSED) {
                    continue;
                }

                if (shouldAbandonLocked(record, tick)) {
                    forceEndLocked(record, EndReason.ABANDONED);
                    continue;
                }

                switch (record.state) {
                    case COUNTDOWN -> {
                        if (stateTick >= record.policy.timing().countdownTicks()) {
                            transitionLocked(record, MatchState.RUNNING, null);
                        }
                    }
                    case RUNNING -> {
                        long timeout = record.policy.timing().runningTimeoutTicks();
                        if (timeout > 0L && stateTick >= timeout) {
                            forceEndLocked(record, EndReason.TIME_LIMIT);
                        }
                    }
                    case ENDING -> {
                        if (stateTick >= record.policy.timing().endingTicks()) {
                            transitionLocked(record, MatchState.RESET, record.lastEndReason);
                        }
                    }
                    case RESET -> closeLocked(record, effectiveEndReason(record));
                    case LOBBY -> {
                        // no automatic transition.
                    }
                }
            }
        }

        stopLoopIfIdle();
    }

    private List<MatchRecord> orderedRecords() {
        return matches.values().stream()
                .sorted(Comparator.comparingLong(MatchRecord::sequence))
                .toList();
    }

    private void expireDisconnectedSessionsLocked(MatchRecord record, long tick) {
        long timeout = record.policy.rejoin().sessionTimeoutTicks();
        if (timeout <= 0L) {
            return;
        }

        for (Map.Entry<UUID, Long> entry : List.copyOf(record.disconnectedSinceTick.entrySet())) {
            long age = Math.max(0L, tick - entry.getValue());
            if (age > timeout) {
                record.disconnectedSinceTick.remove(entry.getKey());
            }
        }
    }

    private boolean shouldAbandonLocked(MatchRecord record, long tick) {
        if (record.state != MatchState.COUNTDOWN && record.state != MatchState.RUNNING) {
            return false;
        }

        if (!record.connectedPlayers.isEmpty()) {
            return false;
        }

        return !hasRejoinablePlayersLocked(record, tick);
    }

    private boolean hasRejoinablePlayersLocked(MatchRecord record, long tick) {
        if (!record.policy.rejoin().enabled()) {
            return false;
        }

        long timeout = record.policy.rejoin().sessionTimeoutTicks();
        long window = record.policy.rejoin().rejoinWindowTicks();

        for (long disconnectedAt : record.disconnectedSinceTick.values()) {
            long age = Math.max(0L, tick - disconnectedAt);
            if (timeout > 0L && age > timeout) {
                continue;
            }
            if (age <= window) {
                return true;
            }
        }

        return false;
    }

    private MatchTransitionResult transitionLocked(MatchRecord record, MatchState target, EndReason reason) {
        if (!invokeStateExit(record)) {
            return MatchTransitionResult.MATCH_CLOSED;
        }

        record.state = target;
        record.stateEnteredTick = record.currentTick;
        if (target == MatchState.ENDING && reason != null) {
            record.lastEndReason = reason;
        }

        if (!invokeStateEnter(record)) {
            return MatchTransitionResult.MATCH_CLOSED;
        }

        return MatchTransitionResult.APPLIED;
    }

    private void forceEndLocked(MatchRecord record, EndReason reason) {
        if (record.status == MatchSessionStatus.CLOSED) {
            return;
        }

        record.lastEndReason = reason;

        if (record.state == MatchState.ENDING) {
            return;
        }
        if (record.state == MatchState.RESET) {
            closeLocked(record, effectiveEndReason(record));
            return;
        }

        transitionLocked(record, MatchState.ENDING, reason);
    }

    private boolean invokeStateEnter(MatchRecord record) {
        try {
            record.callbacks.onStateEnter(record.snapshot());
            return true;
        } catch (RuntimeException ex) {
            logger.error("match callback onStateEnter failed for " + record.matchKey, ex);
            closeLocked(record, EndReason.ERROR);
            return false;
        }
    }

    private boolean invokeStateExit(MatchRecord record) {
        try {
            record.callbacks.onStateExit(record.snapshot());
            return true;
        } catch (RuntimeException ex) {
            logger.error("match callback onStateExit failed for " + record.matchKey, ex);
            closeLocked(record, EndReason.ERROR);
            return false;
        }
    }

    private boolean invokeStateTick(MatchRecord record, long stateTick) {
        try {
            record.callbacks.onStateTick(record.snapshot(), stateTick);
            return true;
        } catch (RuntimeException ex) {
            logger.error("match callback onStateTick failed for " + record.matchKey, ex);
            closeLocked(record, EndReason.ERROR);
            return false;
        }
    }

    private boolean closeLocked(MatchRecord record, EndReason reason) {
        if (record.status == MatchSessionStatus.CLOSED) {
            return false;
        }

        record.status = MatchSessionStatus.CLOSED;
        record.lastEndReason = reason;

        MatchSession closedSnapshot = record.snapshot();

        try {
            record.callbacks.onEnd(closedSnapshot, reason);
        } catch (RuntimeException ex) {
            logger.error("match callback onEnd failed for " + record.matchKey, ex);
        }

        if (!record.cleanupExecuted) {
            record.cleanupExecuted = true;
            try {
                record.cleanup.cleanup(closedSnapshot, reason, services);
            } catch (RuntimeException ex) {
                logger.error("match cleanup failed for " + record.matchKey, ex);
            }
        }

        matches.remove(record.matchId, record);
        stopLoopIfIdle();
        return true;
    }

    private EndReason effectiveEndReason(MatchRecord record) {
        return record.lastEndReason == null ? EndReason.COMPLETED : record.lastEndReason;
    }

    private boolean isValidTransition(MatchState current, MatchState target) {
        return switch (current) {
            case LOBBY -> target == MatchState.COUNTDOWN;
            case COUNTDOWN -> target == MatchState.RUNNING;
            case RUNNING -> target == MatchState.ENDING;
            case ENDING -> target == MatchState.RESET;
            case RESET -> false;
        };
    }

    private boolean isRejoinStateAllowed(MatchState state) {
        return state == MatchState.LOBBY || state == MatchState.COUNTDOWN || state == MatchState.RUNNING;
    }

    private static final class MatchRecord {

        private final UUID matchId;
        private final long sequence;
        private final String matchKey;
        private final dev.patric.commonlib.api.match.MatchPolicy policy;
        private final MatchCallbacks callbacks;
        private final MatchCleanup cleanup;
        private final long createdAtEpochMilli;

        private final Set<UUID> connectedPlayers;
        private final Map<UUID, Long> disconnectedSinceTick;

        private MatchState state;
        private MatchSessionStatus status;
        private long stateEnteredTick;
        private long currentTick;
        private EndReason lastEndReason;
        private boolean cleanupExecuted;

        private MatchRecord(
                UUID matchId,
                long sequence,
                MatchOpenRequest request,
                long createdAtEpochMilli,
                long currentTick
        ) {
            this.matchId = Objects.requireNonNull(matchId, "matchId");
            this.sequence = sequence;
            this.matchKey = request.matchKey();
            this.policy = request.policy();
            this.callbacks = request.callbacks();
            this.cleanup = request.cleanup();
            this.createdAtEpochMilli = createdAtEpochMilli;

            this.connectedPlayers = new HashSet<>(request.initialPlayers());
            this.disconnectedSinceTick = new ConcurrentHashMap<>();

            this.state = MatchState.LOBBY;
            this.status = MatchSessionStatus.ACTIVE;
            this.stateEnteredTick = currentTick;
            this.currentTick = currentTick;
            this.lastEndReason = null;
            this.cleanupExecuted = false;
        }

        private UUID matchId() {
            return matchId;
        }

        private long sequence() {
            return sequence;
        }

        private MatchSession snapshot() {
            return new MatchSession(
                    matchId,
                    matchKey,
                    state,
                    status,
                    policy,
                    Set.copyOf(connectedPlayers),
                    Set.copyOf(disconnectedSinceTick.keySet()),
                    createdAtEpochMilli,
                    stateEnteredTick,
                    currentTick,
                    lastEndReason
            );
        }
    }
}
