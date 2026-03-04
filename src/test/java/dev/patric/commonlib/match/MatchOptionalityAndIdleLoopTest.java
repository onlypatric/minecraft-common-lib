package dev.patric.commonlib.match;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchOptionalityAndIdleLoopTest {

    private ServerMock server;
    private TestPlugin plugin;
    private MatchEngineService matchEngine;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        matchEngine = runtime.services().require(MatchEngineService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void engineIsIdleUntilFirstMatchAndReturnsIdleAfterClose() {
        assertTrue(matchEngine.isIdle());
        assertTrue(matchEngine.active().isEmpty());

        server.getScheduler().performTicks(20L);
        assertTrue(matchEngine.isIdle());
        assertTrue(matchEngine.active().isEmpty());

        matchEngine.open(new MatchOpenRequest(
                "duel.lazy",
                MatchPolicy.competitiveDefaults(),
                new MatchCallbacks() {
                },
                (session, reason, services) -> {
                },
                Set.of()
        ));

        assertTrue(!matchEngine.isIdle());
        assertEquals(1, matchEngine.active().size());

        assertEquals(1, matchEngine.closeAll(EndReason.ADMIN_STOP));
        assertTrue(matchEngine.active().isEmpty());
        assertTrue(matchEngine.isIdle());
    }
}
