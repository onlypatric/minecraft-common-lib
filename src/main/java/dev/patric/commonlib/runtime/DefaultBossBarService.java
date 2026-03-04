package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.hud.BossBarOpenRequest;
import dev.patric.commonlib.api.hud.BossBarService;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.BossBarUpdateResult;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudUpdatePolicy;
import dev.patric.commonlib.api.port.BossBarPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default bossbar service with throttled rendering.
 */
public final class DefaultBossBarService implements BossBarService {

    private final Map<UUID, BarRecord> bars;
    private final CommonScheduler scheduler;
    private final RuntimeLogger logger;
    private final BossBarPort port;
    private final HudUpdatePolicy policy;
    private final AtomicLong tickCounter;
    private final Object flushLock;
    private volatile TaskHandle flushHandle;

    /**
     * Creates bossbar service with competitive default policy.
     *
     * @param scheduler scheduler facade.
     * @param logger runtime logger.
     * @param port bossbar port.
     */
    public DefaultBossBarService(CommonScheduler scheduler, RuntimeLogger logger, BossBarPort port) {
        this(scheduler, logger, port, HudUpdatePolicy.competitiveDefaults());
    }

    /**
     * Creates bossbar service with explicit policy.
     *
     * @param scheduler scheduler facade.
     * @param logger runtime logger.
     * @param port bossbar port.
     * @param policy update policy.
     */
    public DefaultBossBarService(
            CommonScheduler scheduler,
            RuntimeLogger logger,
            BossBarPort port,
            HudUpdatePolicy policy
    ) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.port = Objects.requireNonNull(port, "port");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.bars = new ConcurrentHashMap<>();
        this.tickCounter = new AtomicLong(0L);
        this.flushLock = new Object();
        this.flushHandle = null;
    }

    @Override
    public BossBarSession open(BossBarOpenRequest request) {
        Objects.requireNonNull(request, "request");
        if (!ensureFlushLoopStarted()) {
            throw new IllegalStateException("Unable to start bossbar flush loop");
        }

        UUID barId = UUID.randomUUID();
        long now = System.currentTimeMillis();
        BarRecord record = new BarRecord(barId, request.playerId(), request.barKey(), request.initialState(), now);
        bars.put(barId, record);

        BossBarSession session = record.snapshot();
        if (!safeOpen(session)) {
            closeInternal(barId, HudAudienceCloseReason.ERROR);
            return record.snapshotClosed();
        }

        long tick = tickCounter.get();
        if (!safeRender(barId, request.initialState())) {
            closeInternal(barId, HudAudienceCloseReason.ERROR);
            return record.snapshotClosed();
        }

        record.markRendered(request.initialState(), tick);
        return record.snapshot();
    }

    @Override
    public Optional<BossBarSession> find(UUID barId) {
        Objects.requireNonNull(barId, "barId");
        BarRecord record = bars.get(barId);
        return record == null ? Optional.empty() : Optional.of(record.snapshot());
    }

    @Override
    public List<BossBarSession> activeByPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        List<BossBarSession> active = new ArrayList<>();
        for (BarRecord record : bars.values()) {
            if (record.belongsTo(playerId) && record.isOpen()) {
                active.add(record.snapshot());
            }
        }
        return List.copyOf(active);
    }

    @Override
    public BossBarUpdateResult update(UUID barId, BossBarState nextState) {
        Objects.requireNonNull(barId, "barId");
        Objects.requireNonNull(nextState, "nextState");
        if (!ensureFlushLoopStarted()) {
            return BossBarUpdateResult.BAR_CLOSED;
        }

        BossBarUpdateResult validity = validateState(nextState);
        if (validity != BossBarUpdateResult.APPLIED) {
            return validity;
        }

        BarRecord record = bars.get(barId);
        if (record == null) {
            return BossBarUpdateResult.BAR_NOT_FOUND;
        }

        long tick = tickCounter.get();
        UpdateDecision decision = record.prepareUpdate(nextState, tick, policy);
        if (decision.result() != BossBarUpdateResult.APPLIED) {
            return decision.result();
        }

        if (!safeRender(barId, decision.state())) {
            closeInternal(barId, HudAudienceCloseReason.ERROR);
            return BossBarUpdateResult.BAR_CLOSED;
        }

        record.markRendered(decision.state(), tick);
        return BossBarUpdateResult.APPLIED;
    }

    @Override
    public boolean close(UUID barId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(barId, "barId");
        Objects.requireNonNull(reason, "reason");
        return closeInternal(barId, reason);
    }

    @Override
    public int closeAllByPlayer(UUID playerId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (BarRecord record : List.copyOf(bars.values())) {
            if (record.belongsTo(playerId) && closeInternal(record.barId(), reason)) {
                closed++;
            }
        }
        return closed;
    }

    @Override
    public int closeAll(HudAudienceCloseReason reason) {
        Objects.requireNonNull(reason, "reason");

        int closed = 0;
        for (UUID barId : List.copyOf(bars.keySet())) {
            if (closeInternal(barId, reason)) {
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
        if (bars.isEmpty()) {
            stopFlushLoopIfIdle();
            return;
        }

        long tick = tickCounter.incrementAndGet();

        for (BarRecord record : List.copyOf(bars.values())) {
            PendingRender pending = record.takeRenderablePending(tick, policy);
            if (pending == null) {
                continue;
            }

            if (!safeRender(pending.barId(), pending.state())) {
                closeInternal(pending.barId(), HudAudienceCloseReason.ERROR);
                continue;
            }

            record.markRendered(pending.state(), tick);
        }
    }

    private boolean closeInternal(UUID barId, HudAudienceCloseReason reason) {
        BarRecord record = bars.get(barId);
        if (record == null) {
            return false;
        }
        if (!record.beginClose()) {
            return false;
        }

        if (!safeClose(barId, reason)) {
            logger.warn("bossbar close rejected by backend for " + barId + " with reason " + reason);
        }

        bars.remove(barId, record);
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
                logger.error("bossbar flush loop start failed", ex);
                return false;
            }
        }
    }

    private void stopFlushLoopIfIdle() {
        if (!bars.isEmpty()) {
            return;
        }
        synchronized (flushLock) {
            if (!bars.isEmpty()) {
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

    private BossBarUpdateResult validateState(BossBarState state) {
        if (state.title().isBlank()) {
            return BossBarUpdateResult.INVALID_STATE;
        }
        try {
            new BossBarState(state.title(), state.progress(), state.color(), state.style(), state.visible());
            return BossBarUpdateResult.APPLIED;
        } catch (RuntimeException ex) {
            return BossBarUpdateResult.INVALID_STATE;
        }
    }

    private boolean safeOpen(BossBarSession session) {
        try {
            return port.open(session);
        } catch (RuntimeException ex) {
            logger.error("bossbar open threw exception", ex);
            return false;
        }
    }

    private boolean safeRender(UUID barId, BossBarState state) {
        try {
            return port.render(barId, state);
        } catch (RuntimeException ex) {
            logger.error("bossbar render threw exception", ex);
            return false;
        }
    }

    private boolean safeClose(UUID barId, HudAudienceCloseReason reason) {
        try {
            return port.close(barId, reason);
        } catch (RuntimeException ex) {
            logger.error("bossbar close threw exception", ex);
            return false;
        }
    }

    private record PendingRender(UUID barId, BossBarState state) {
    }

    private record UpdateDecision(BossBarUpdateResult result, BossBarState state) {
    }

    private static final class BarRecord {

        private final UUID barId;
        private final UUID playerId;
        private final String barKey;
        private final long openedAtEpochMilli;

        private BossBarState rendered;
        private BossBarState pending;
        private boolean closed;
        private long lastRenderedTick;

        private BarRecord(UUID barId, UUID playerId, String barKey, BossBarState initial, long openedAtEpochMilli) {
            this.barId = Objects.requireNonNull(barId, "barId");
            this.playerId = Objects.requireNonNull(playerId, "playerId");
            this.barKey = Objects.requireNonNull(barKey, "barKey");
            this.rendered = Objects.requireNonNull(initial, "initial");
            this.pending = null;
            this.closed = false;
            this.openedAtEpochMilli = openedAtEpochMilli;
            this.lastRenderedTick = -1L;
        }

        private synchronized UUID barId() {
            return barId;
        }

        private synchronized boolean belongsTo(UUID playerId) {
            return this.playerId.equals(playerId);
        }

        private synchronized boolean isOpen() {
            return !closed;
        }

        private synchronized BossBarSession snapshot() {
            return new BossBarSession(
                    barId,
                    playerId,
                    barKey,
                    rendered,
                    closed,
                    openedAtEpochMilli,
                    lastRenderedTick
            );
        }

        private synchronized BossBarSession snapshotClosed() {
            return new BossBarSession(
                    barId,
                    playerId,
                    barKey,
                    rendered,
                    true,
                    openedAtEpochMilli,
                    lastRenderedTick
            );
        }

        private synchronized UpdateDecision prepareUpdate(BossBarState next, long tick, HudUpdatePolicy policy) {
            if (closed) {
                return new UpdateDecision(BossBarUpdateResult.BAR_CLOSED, null);
            }

            if (policy.deduplicatePayload() && (next.equals(rendered) || next.equals(pending))) {
                return new UpdateDecision(BossBarUpdateResult.DEDUPED, null);
            }

            if (lastRenderedTick >= 0L && tick - lastRenderedTick < policy.minUpdateIntervalTicks()) {
                pending = next;
                return new UpdateDecision(BossBarUpdateResult.THROTTLED, null);
            }

            return new UpdateDecision(BossBarUpdateResult.APPLIED, next);
        }

        private synchronized PendingRender takeRenderablePending(long tick, HudUpdatePolicy policy) {
            if (closed || pending == null) {
                return null;
            }
            if (lastRenderedTick >= 0L && tick - lastRenderedTick < policy.minUpdateIntervalTicks()) {
                return null;
            }
            if (policy.deduplicatePayload() && pending.equals(rendered)) {
                pending = null;
                return null;
            }
            return new PendingRender(barId, pending);
        }

        private synchronized void markRendered(BossBarState state, long tick) {
            if (closed) {
                return;
            }
            rendered = state;
            if (Objects.equals(pending, state)) {
                pending = null;
            }
            lastRenderedTick = tick;
        }

        private synchronized boolean beginClose() {
            if (closed) {
                return false;
            }
            closed = true;
            pending = null;
            return true;
        }
    }
}
