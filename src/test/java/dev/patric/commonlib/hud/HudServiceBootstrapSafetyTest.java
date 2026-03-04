package dev.patric.commonlib.hud;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.hud.BossBarOpenRequest;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.runtime.DefaultBossBarService;
import dev.patric.commonlib.runtime.DefaultScoreboardSessionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HudServiceBootstrapSafetyTest {

    @Test
    void scoreboardServiceStartsFlushLoopLazily() {
        CommonScheduler scheduler = mock(CommonScheduler.class);
        RuntimeLogger logger = mock(RuntimeLogger.class);
        ScoreboardPort port = mock(ScoreboardPort.class);
        when(scheduler.runSyncRepeating(anyLong(), anyLong(), any())).thenReturn(new NoopTaskHandle());
        when(port.open(any())).thenReturn(true);
        when(port.render(any(), any())).thenReturn(true);

        DefaultScoreboardSessionService service = new DefaultScoreboardSessionService(scheduler, logger, port);
        verifyNoInteractions(scheduler);

        service.open(new ScoreboardOpenRequest(
                UUID.randomUUID(),
                "board.bootstrap",
                new ScoreboardSnapshot("Title", List.of("line"))
        ));

        verify(scheduler, times(1)).runSyncRepeating(eq(1L), eq(1L), any());
    }

    @Test
    void bossBarServiceStartsFlushLoopLazily() {
        CommonScheduler scheduler = mock(CommonScheduler.class);
        RuntimeLogger logger = mock(RuntimeLogger.class);
        BossBarPort port = mock(BossBarPort.class);
        when(scheduler.runSyncRepeating(anyLong(), anyLong(), any())).thenReturn(new NoopTaskHandle());
        when(port.open(any())).thenReturn(true);
        when(port.render(any(), any())).thenReturn(true);

        DefaultBossBarService service = new DefaultBossBarService(scheduler, logger, port);
        verify(scheduler, never()).runSyncRepeating(anyLong(), anyLong(), any());

        service.open(new BossBarOpenRequest(
                UUID.randomUUID(),
                "boss.bootstrap",
                new BossBarState("Boss", 1.0f, HudBarColor.RED, HudBarStyle.SOLID, true)
        ));

        verify(scheduler, times(1)).runSyncRepeating(eq(1L), eq(1L), any());
    }

    private static final class NoopTaskHandle implements TaskHandle {

        @Override
        public void cancel() {
            // no-op
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
