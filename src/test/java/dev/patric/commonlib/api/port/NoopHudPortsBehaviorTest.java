package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.noop.NoopBossBarPort;
import dev.patric.commonlib.api.port.noop.NoopScoreboardPort;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoopHudPortsBehaviorTest {

    @Test
    void noopScoreboardAndBossBarPortsReturnSafeDeterministicValues() {
        NoopScoreboardPort scoreboardPort = new NoopScoreboardPort();
        NoopBossBarPort bossBarPort = new NoopBossBarPort();

        ScoreboardSession scoreboardSession = new ScoreboardSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "board.main",
                new ScoreboardSnapshot("Title", List.of("L1")),
                ScoreboardSessionStatus.OPEN,
                1L,
                0L
        );

        BossBarSession bossBarSession = new BossBarSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "bar.main",
                new BossBarState("HP", 1.0f, HudBarColor.GREEN, HudBarStyle.SOLID, true),
                false,
                1L,
                0L
        );

        assertTrue(scoreboardPort.open(scoreboardSession));
        assertTrue(scoreboardPort.render(scoreboardSession.sessionId(), scoreboardSession.snapshot()));
        assertTrue(scoreboardPort.close(scoreboardSession.sessionId(), HudAudienceCloseReason.MANUAL));

        assertTrue(bossBarPort.open(bossBarSession));
        assertTrue(bossBarPort.render(bossBarSession.barId(), bossBarSession.state()));
        assertTrue(bossBarPort.close(bossBarSession.barId(), HudAudienceCloseReason.MANUAL));
    }
}
