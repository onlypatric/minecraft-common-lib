package dev.patric.commonlib.hud;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.hud.HudUpdatePolicy;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.hud.ScoreboardUpdateResult;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.runtime.DefaultScoreboardSessionService;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardRateLimitTest {

    private ServerMock server;
    private TestPlugin plugin;
    private CommonScheduler scheduler;
    private RuntimeLogger logger;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        scheduler = runtime.services().require(CommonScheduler.class);
        logger = runtime.services().require(RuntimeLogger.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void scoreboardUpdatesAreThrottledAndDeduplicated() {
        CountingScoreboardPort port = new CountingScoreboardPort();
        DefaultScoreboardSessionService service = new DefaultScoreboardSessionService(
                scheduler,
                logger,
                port,
                HudUpdatePolicy.competitiveDefaults()
        );

        UUID playerId = UUID.randomUUID();
        ScoreboardSession session = service.open(new ScoreboardOpenRequest(
                playerId,
                "board.rate",
                new ScoreboardSnapshot("Arena", List.of("A"))
        ));

        assertEquals(1, port.renderCount(session.sessionId()));

        ScoreboardUpdateResult throttled = service.update(
                session.sessionId(),
                new ScoreboardSnapshot("Arena", List.of("B"))
        );
        assertEquals(ScoreboardUpdateResult.THROTTLED, throttled);
        assertEquals(1, port.renderCount(session.sessionId()));

        server.getScheduler().performTicks(4L);
        assertEquals(1, port.renderCount(session.sessionId()));

        server.getScheduler().performTicks(1L);
        assertEquals(2, port.renderCount(session.sessionId()));

        ScoreboardUpdateResult deduped = service.update(
                session.sessionId(),
                new ScoreboardSnapshot("Arena", List.of("B"))
        );
        assertEquals(ScoreboardUpdateResult.DEDUPED, deduped);
        assertEquals(2, port.renderCount(session.sessionId()));
    }

    @Test
    void throttleCapsRenderCountAcrossManyAudiences() {
        CountingScoreboardPort port = new CountingScoreboardPort();
        DefaultScoreboardSessionService service = new DefaultScoreboardSessionService(
                scheduler,
                logger,
                port,
                HudUpdatePolicy.competitiveDefaults()
        );

        int audiences = 100;
        List<ScoreboardSession> sessions = java.util.stream.IntStream.range(0, audiences)
                .mapToObj(i -> service.open(new ScoreboardOpenRequest(
                        UUID.randomUUID(),
                        "board." + i,
                        new ScoreboardSnapshot("T", List.of("L0"))
                )))
                .toList();

        for (int tick = 0; tick < 10; tick++) {
            for (int i = 0; i < audiences; i++) {
                service.update(sessions.get(i).sessionId(), new ScoreboardSnapshot("T", List.of("L" + tick)));
            }
            server.getScheduler().performTicks(1L);
        }

        int totalRenders = port.totalRenders();
        // open render (100) + at most two throttled flush renders per audience in 10 ticks (<=200)
        assertTrue(totalRenders <= 300, "totalRenders=" + totalRenders);
    }

    private static final class CountingScoreboardPort implements ScoreboardPort {

        private final Map<UUID, AtomicInteger> renderCounts = new ConcurrentHashMap<>();

        @Override
        public boolean open(ScoreboardSession session) {
            renderCounts.putIfAbsent(session.sessionId(), new AtomicInteger());
            return true;
        }

        @Override
        public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
            renderCounts.computeIfAbsent(sessionId, ignored -> new AtomicInteger()).incrementAndGet();
            return true;
        }

        @Override
        public boolean close(UUID sessionId, dev.patric.commonlib.api.hud.HudAudienceCloseReason reason) {
            return true;
        }

        int renderCount(UUID sessionId) {
            AtomicInteger counter = renderCounts.get(sessionId);
            return counter == null ? 0 : counter.get();
        }

        int totalRenders() {
            return renderCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        }
    }
}
