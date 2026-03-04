package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudUpdatePolicy;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionService;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.hud.ScoreboardUpdateResult;
import dev.patric.commonlib.api.port.ScoreboardPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default scoreboard session service with throttled rendering.
 */
public final class DefaultScoreboardSessionService implements ScoreboardSessionService {

    private final Map<UUID, SessionRecord> sessions;
    private final CommonScheduler scheduler;
    private final RuntimeLogger logger;
    private final ScoreboardPort port;
    private final HudUpdatePolicy policy;
    private final AtomicLong tickCounter;
    private final Object flushLock;
    private volatile TaskHandle flushHandle;

    /**
     * Creates scoreboard service with competitive default policy.
     *
     * @param scheduler scheduler facade.
     * @param logger runtime logger.
     * @param port scoreboard port.
     */
    public DefaultScoreboardSessionService(CommonScheduler scheduler, RuntimeLogger logger, ScoreboardPort port) {
        this(scheduler, logger, port, HudUpdatePolicy.competitiveDefaults());
    }

    /**
     * Creates scoreboard service with explicit policy.
     *
     * @param scheduler scheduler facade.
     * @param logger runtime logger.
     * @param port scoreboard port.
     * @param policy update policy.
     */
    public DefaultScoreboardSessionService(
            CommonScheduler scheduler,
            RuntimeLogger logger,
            ScoreboardPort port,
            HudUpdatePolicy policy
    ) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.port = Objects.requireNonNull(port, "port");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.sessions = new ConcurrentHashMap<>();
        this.tickCounter = new AtomicLong(0L);
        this.flushLock = new Object();
        this.flushHandle = null;
    }

    @Override
    public ScoreboardSession open(ScoreboardOpenRequest request) {
        Objects.requireNonNull(request, "request");
        if (!ensureFlushLoopStarted()) {
            throw new IllegalStateException("Unable to start scoreboard flush loop");
        }

        ScoreboardSnapshot normalized = normalizeOpenSnapshot(request.initialSnapshot(), request.boardKey());
        UUID sessionId = UUID.randomUUID();
        long now = System.currentTimeMillis();
        SessionRecord record = new SessionRecord(sessionId, request.playerId(), request.boardKey(), normalized, now);
        sessions.put(sessionId, record);

        ScoreboardSession session = record.snapshot();
        if (!safeOpen(session)) {
            closeInternal(sessionId, HudAudienceCloseReason.ERROR);
            return record.snapshotClosed();
        }

        long tick = tickCounter.get();
        if (!safeRender(sessionId, normalized)) {
            closeInternal(sessionId, HudAudienceCloseReason.ERROR);
            return record.snapshotClosed();
        }

        record.markRendered(normalized, tick);
        return record.snapshot();
    }

    @Override
    public Optional<ScoreboardSession> find(UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        SessionRecord record = sessions.get(sessionId);
        return record == null ? Optional.empty() : Optional.of(record.snapshot());
    }

    @Override
    public List<ScoreboardSession> activeByPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        List<ScoreboardSession> active = new ArrayList<>();
        for (SessionRecord record : sessions.values()) {
            if (record.belongsTo(playerId) && record.isOpen()) {
                active.add(record.snapshot());
            }
        }
        return List.copyOf(active);
    }

    @Override
    public ScoreboardUpdateResult update(UUID sessionId, ScoreboardSnapshot nextSnapshot) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(nextSnapshot, "nextSnapshot");
        if (!ensureFlushLoopStarted()) {
            return ScoreboardUpdateResult.SESSION_CLOSED;
        }

        ScoreboardUpdateResult validity = validateUpdatePayload(nextSnapshot);
        if (validity != ScoreboardUpdateResult.APPLIED) {
            return validity;
        }

        SessionRecord record = sessions.get(sessionId);
        if (record == null) {
            return ScoreboardUpdateResult.SESSION_NOT_FOUND;
        }

        long tick = tickCounter.get();
        UpdateDecision decision = record.prepareUpdate(nextSnapshot, tick, policy);
        if (decision.result() != ScoreboardUpdateResult.APPLIED) {
            return decision.result();
        }

        if (!safeRender(sessionId, decision.snapshot())) {
            closeInternal(sessionId, HudAudienceCloseReason.ERROR);
            return ScoreboardUpdateResult.SESSION_CLOSED;
        }

        record.markRendered(decision.snapshot(), tick);
        return ScoreboardUpdateResult.APPLIED;
    }

    @Override
    public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        return closeInternal(sessionId, reason);
    }

    @Override
    public int closeAllByPlayer(UUID playerId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (SessionRecord record : List.copyOf(sessions.values())) {
            if (record.belongsTo(playerId) && closeInternal(record.sessionId(), reason)) {
                closed++;
            }
        }
        return closed;
    }

    @Override
    public int closeAll(HudAudienceCloseReason reason) {
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (UUID sessionId : List.copyOf(sessions.keySet())) {
            if (closeInternal(sessionId, reason)) {
                closed++;
            }
        }
        return closed;
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        closeAllByPlayer(playerId, HudAudienceCloseReason.QUIT);
    }

    @Override
    public void onPlayerWorldChange(UUID playerId) {
        closeAllByPlayer(playerId, HudAudienceCloseReason.WORLD_CHANGE);
    }

    @Override
    public HudUpdatePolicy policy() {
        return policy;
    }

    private void flushTick() {
        if (sessions.isEmpty()) {
            stopFlushLoopIfIdle();
            return;
        }

        long tick = tickCounter.incrementAndGet();

        for (SessionRecord record : List.copyOf(sessions.values())) {
            PendingRender pending = record.takeRenderablePending(tick, policy);
            if (pending == null) {
                continue;
            }

            if (!safeRender(pending.sessionId(), pending.snapshot())) {
                closeInternal(pending.sessionId(), HudAudienceCloseReason.ERROR);
                continue;
            }

            record.markRendered(pending.snapshot(), tick);
        }
    }

    private boolean closeInternal(UUID sessionId, HudAudienceCloseReason reason) {
        SessionRecord record = sessions.get(sessionId);
        if (record == null) {
            return false;
        }
        if (!record.beginClose()) {
            return false;
        }

        if (!safeClose(sessionId, reason)) {
            logger.warn("scoreboard close rejected by backend for session " + sessionId + " with reason " + reason);
        }

        sessions.remove(sessionId, record);
        stopFlushLoopIfIdle();
        return true;
    }

    private boolean ensureFlushLoopStarted() {
        if (flushHandle != null && !flushHandle.isCancelled()) {
            return true;
        }
        synchronized (flushLock) {
            if (flushHandle != null && !flushHandle.isCancelled()) {
                return true;
            }
            try {
                flushHandle = scheduler.runSyncRepeating(1L, 1L, this::flushTick);
                return true;
            } catch (RuntimeException ex) {
                logger.error("scoreboard flush loop start failed", ex);
                return false;
            }
        }
    }

    private void stopFlushLoopIfIdle() {
        if (!sessions.isEmpty()) {
            return;
        }
        synchronized (flushLock) {
            if (!sessions.isEmpty()) {
                return;
            }
            TaskHandle handle = flushHandle;
            if (handle == null) {
                return;
            }
            handle.cancel();
            flushHandle = null;
        }
    }

    private ScoreboardSnapshot normalizeOpenSnapshot(ScoreboardSnapshot snapshot, String boardKey) {
        String normalizedTitle = snapshot.title().isBlank() ? boardKey : snapshot.title();
        List<String> lines = snapshot.lines();
        if (lines.size() > policy.maxScoreboardLines()) {
            lines = lines.subList(0, policy.maxScoreboardLines());
        }
        return new ScoreboardSnapshot(normalizedTitle, lines);
    }

    private ScoreboardUpdateResult validateUpdatePayload(ScoreboardSnapshot snapshot) {
        if (snapshot.title().isBlank()) {
            return ScoreboardUpdateResult.INVALID_PAYLOAD;
        }
        if (snapshot.lines().size() > policy.maxScoreboardLines()) {
            return ScoreboardUpdateResult.INVALID_PAYLOAD;
        }
        return ScoreboardUpdateResult.APPLIED;
    }

    private boolean safeOpen(ScoreboardSession session) {
        try {
            return port.open(session);
        } catch (RuntimeException ex) {
            logger.error("scoreboard open threw exception", ex);
            return false;
        }
    }

    private boolean safeRender(UUID sessionId, ScoreboardSnapshot snapshot) {
        try {
            return port.render(sessionId, snapshot);
        } catch (RuntimeException ex) {
            logger.error("scoreboard render threw exception", ex);
            return false;
        }
    }

    private boolean safeClose(UUID sessionId, HudAudienceCloseReason reason) {
        try {
            return port.close(sessionId, reason);
        } catch (RuntimeException ex) {
            logger.error("scoreboard close threw exception", ex);
            return false;
        }
    }

    private record PendingRender(UUID sessionId, ScoreboardSnapshot snapshot) {
    }

    private record UpdateDecision(ScoreboardUpdateResult result, ScoreboardSnapshot snapshot) {
    }

    private static final class SessionRecord {

        private final UUID sessionId;
        private final UUID playerId;
        private final String boardKey;
        private final long openedAtEpochMilli;

        private ScoreboardSnapshot rendered;
        private ScoreboardSnapshot pending;
        private ScoreboardSessionStatus status;
        private long lastRenderedTick;

        private SessionRecord(
                UUID sessionId,
                UUID playerId,
                String boardKey,
                ScoreboardSnapshot initial,
                long openedAtEpochMilli
        ) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.playerId = Objects.requireNonNull(playerId, "playerId");
            this.boardKey = Objects.requireNonNull(boardKey, "boardKey");
            this.openedAtEpochMilli = openedAtEpochMilli;
            this.rendered = Objects.requireNonNull(initial, "initial");
            this.pending = null;
            this.status = ScoreboardSessionStatus.OPEN;
            this.lastRenderedTick = -1L;
        }

        private synchronized UUID sessionId() {
            return sessionId;
        }

        private synchronized boolean belongsTo(UUID playerId) {
            return this.playerId.equals(playerId);
        }

        private synchronized boolean isOpen() {
            return status == ScoreboardSessionStatus.OPEN;
        }

        private synchronized ScoreboardSession snapshot() {
            return new ScoreboardSession(
                    sessionId,
                    playerId,
                    boardKey,
                    rendered,
                    status,
                    openedAtEpochMilli,
                    lastRenderedTick
            );
        }

        private synchronized ScoreboardSession snapshotClosed() {
            return new ScoreboardSession(
                    sessionId,
                    playerId,
                    boardKey,
                    rendered,
                    ScoreboardSessionStatus.CLOSED,
                    openedAtEpochMilli,
                    lastRenderedTick
            );
        }

        private synchronized UpdateDecision prepareUpdate(ScoreboardSnapshot next, long tick, HudUpdatePolicy policy) {
            if (status != ScoreboardSessionStatus.OPEN) {
                return new UpdateDecision(ScoreboardUpdateResult.SESSION_CLOSED, null);
            }

            if (policy.deduplicatePayload() && (next.equals(rendered) || next.equals(pending))) {
                return new UpdateDecision(ScoreboardUpdateResult.DEDUPED, null);
            }

            if (lastRenderedTick >= 0L && tick - lastRenderedTick < policy.minUpdateIntervalTicks()) {
                pending = next;
                return new UpdateDecision(ScoreboardUpdateResult.THROTTLED, null);
            }

            return new UpdateDecision(ScoreboardUpdateResult.APPLIED, next);
        }

        private synchronized PendingRender takeRenderablePending(long tick, HudUpdatePolicy policy) {
            if (status != ScoreboardSessionStatus.OPEN || pending == null) {
                return null;
            }
            if (lastRenderedTick >= 0L && tick - lastRenderedTick < policy.minUpdateIntervalTicks()) {
                return null;
            }
            if (policy.deduplicatePayload() && pending.equals(rendered)) {
                pending = null;
                return null;
            }
            return new PendingRender(sessionId, pending);
        }

        private synchronized void markRendered(ScoreboardSnapshot snapshot, long tick) {
            if (status != ScoreboardSessionStatus.OPEN) {
                return;
            }
            rendered = snapshot;
            if (Objects.equals(pending, snapshot)) {
                pending = null;
            }
            lastRenderedTick = tick;
        }

        private synchronized boolean beginClose() {
            if (status != ScoreboardSessionStatus.OPEN) {
                return false;
            }
            status = ScoreboardSessionStatus.CLOSED;
            pending = null;
            return true;
        }
    }
}
