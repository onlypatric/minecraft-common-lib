package dev.patric.commonlib.api.hud;

import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudApiContractTest {

    @Test
    void scoreboardSessionServiceContractMatchesExpectedSignatures() throws Exception {
        assertMethod(ScoreboardSessionService.class, "open", ScoreboardSession.class, ScoreboardOpenRequest.class);
        assertMethod(ScoreboardSessionService.class, "find", Optional.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "update", ScoreboardUpdateResult.class, UUID.class, ScoreboardSnapshot.class);
        assertMethod(ScoreboardSessionService.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(ScoreboardSessionService.class, "closeAllByPlayer", int.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(ScoreboardSessionService.class, "closeAll", int.class, HudAudienceCloseReason.class);
        assertMethod(ScoreboardSessionService.class, "onPlayerQuit", void.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "onPlayerWorldChange", void.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "policy", HudUpdatePolicy.class);
    }

    @Test
    void bossBarServiceContractMatchesExpectedSignatures() throws Exception {
        assertMethod(BossBarService.class, "open", BossBarSession.class, BossBarOpenRequest.class);
        assertMethod(BossBarService.class, "find", Optional.class, UUID.class);
        assertMethod(BossBarService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(BossBarService.class, "update", BossBarUpdateResult.class, UUID.class, BossBarState.class);
        assertMethod(BossBarService.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(BossBarService.class, "closeAllByPlayer", int.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(BossBarService.class, "closeAll", int.class, HudAudienceCloseReason.class);
        assertMethod(BossBarService.class, "onPlayerQuit", void.class, UUID.class);
        assertMethod(BossBarService.class, "onPlayerWorldChange", void.class, UUID.class);
        assertMethod(BossBarService.class, "policy", HudUpdatePolicy.class);
    }

    @Test
    void hudPortsContractMatchesExpectedSignatures() throws Exception {
        assertMethod(ScoreboardPort.class, "open", boolean.class, ScoreboardSession.class);
        assertMethod(ScoreboardPort.class, "render", boolean.class, UUID.class, ScoreboardSnapshot.class);
        assertMethod(ScoreboardPort.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);

        assertMethod(BossBarPort.class, "open", boolean.class, BossBarSession.class);
        assertMethod(BossBarPort.class, "render", boolean.class, UUID.class, BossBarState.class);
        assertMethod(BossBarPort.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType());
    }
}
