package dev.patric.commonlib.hud;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.hud.BossBarOpenRequest;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.hud.HudUpdatePolicy;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.runtime.DefaultBossBarService;
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

class HudTeardownLeakTest {

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
    void closeAllPreventsFurtherScoreboardRenders() {
        CountingScoreboardPort port = new CountingScoreboardPort();
        DefaultScoreboardSessionService service = new DefaultScoreboardSessionService(
                scheduler,
                logger,
                port,
                HudUpdatePolicy.competitiveDefaults()
        );

        ScoreboardSession session = service.open(new ScoreboardOpenRequest(
                UUID.randomUUID(),
                "sb.teardown",
                new ScoreboardSnapshot("T", List.of("0"))
        ));

        service.update(session.sessionId(), new ScoreboardSnapshot("T", List.of("1")));
        service.closeAll(HudAudienceCloseReason.MANUAL);

        int rendersBefore = port.totalRenders();
        server.getScheduler().performTicks(20L);
        assertEquals(rendersBefore, port.totalRenders());
    }

    @Test
    void closeAllPreventsFurtherBossBarRenders() {
        CountingBossBarPort port = new CountingBossBarPort();
        DefaultBossBarService service = new DefaultBossBarService(
                scheduler,
                logger,
                port,
                HudUpdatePolicy.competitiveDefaults()
        );

        BossBarSession session = service.open(new BossBarOpenRequest(
                UUID.randomUUID(),
                "bar.teardown",
                new BossBarState("HP", 1.0f, HudBarColor.RED, HudBarStyle.SOLID, true)
        ));

        service.update(session.barId(), new BossBarState("HP", 0.8f, HudBarColor.YELLOW, HudBarStyle.SOLID, true));
        service.closeAll(HudAudienceCloseReason.MANUAL);

        int rendersBefore = port.totalRenders();
        server.getScheduler().performTicks(20L);
        assertEquals(rendersBefore, port.totalRenders());
    }

    private static final class CountingScoreboardPort implements ScoreboardPort {

        private final Map<UUID, AtomicInteger> counters = new ConcurrentHashMap<>();

        @Override
        public boolean open(ScoreboardSession session) {
            counters.putIfAbsent(session.sessionId(), new AtomicInteger());
            return true;
        }

        @Override
        public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
            counters.computeIfAbsent(sessionId, ignored -> new AtomicInteger()).incrementAndGet();
            return true;
        }

        @Override
        public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
            return true;
        }

        int totalRenders() {
            return counters.values().stream().mapToInt(AtomicInteger::get).sum();
        }
    }

    private static final class CountingBossBarPort implements BossBarPort {

        private final Map<UUID, AtomicInteger> counters = new ConcurrentHashMap<>();

        @Override
        public boolean open(BossBarSession session) {
            counters.putIfAbsent(session.barId(), new AtomicInteger());
            return true;
        }

        @Override
        public boolean render(UUID barId, BossBarState state) {
            counters.computeIfAbsent(barId, ignored -> new AtomicInteger()).incrementAndGet();
            return true;
        }

        @Override
        public boolean close(UUID barId, HudAudienceCloseReason reason) {
            return true;
        }

        int totalRenders() {
            return counters.values().stream().mapToInt(AtomicInteger::get).sum();
        }
    }
}
