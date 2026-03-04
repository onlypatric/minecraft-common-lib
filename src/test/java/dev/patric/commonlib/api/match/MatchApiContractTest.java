package dev.patric.commonlib.api.match;

import dev.patric.commonlib.api.ServiceRegistry;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchApiContractTest {

    @Test
    void matchApiTypesExposeExpectedContracts() throws Exception {
        assertEquals(Set.of(MatchState.LOBBY, MatchState.COUNTDOWN, MatchState.RUNNING, MatchState.ENDING, MatchState.RESET),
                Set.of(MatchState.values()));
        assertEquals(Set.of(
                        EndReason.COMPLETED,
                        EndReason.TIME_LIMIT,
                        EndReason.ABANDONED,
                        EndReason.ADMIN_STOP,
                        EndReason.PLUGIN_DISABLE,
                        EndReason.ERROR
                ),
                Set.of(EndReason.values()));

        assertMethod(MatchEngineService.class, "open", MatchSession.class, MatchOpenRequest.class);
        assertMethod(MatchEngineService.class, "find", Optional.class, UUID.class);
        assertMethod(MatchEngineService.class, "active", List.class);
        assertMethod(MatchEngineService.class, "startCountdown", MatchTransitionResult.class, UUID.class);
        assertMethod(MatchEngineService.class, "transition", MatchTransitionResult.class, UUID.class, MatchState.class, EndReason.class);
        assertMethod(MatchEngineService.class, "end", MatchTransitionResult.class, UUID.class, EndReason.class);
        assertMethod(MatchEngineService.class, "join", JoinResult.class, UUID.class, UUID.class);
        assertMethod(MatchEngineService.class, "disconnect", DisconnectResult.class, UUID.class, UUID.class);
        assertMethod(MatchEngineService.class, "rejoin", RejoinResult.class, UUID.class, UUID.class);
        assertMethod(MatchEngineService.class, "closeAll", int.class, EndReason.class);
        assertMethod(MatchEngineService.class, "onPlayerQuit", void.class, UUID.class);
        assertMethod(MatchEngineService.class, "onPlayerWorldChange", void.class, UUID.class);
        assertMethod(MatchEngineService.class, "isIdle", boolean.class);

        assertMethod(MatchCleanup.class, "cleanup", void.class, MatchSession.class, EndReason.class, ServiceRegistry.class);
        assertMethod(MatchCleanup.class, "noop", MatchCleanup.class);
        assertMethod(MatchTimingPolicy.class, "competitiveDefaults", MatchTimingPolicy.class);
        assertMethod(RejoinPolicy.class, "competitiveDefaults", RejoinPolicy.class);
        assertMethod(MatchPolicy.class, "competitiveDefaults", MatchPolicy.class);

        MatchCallbacks callbacks = new MatchCallbacks() {
        };
        MatchCleanup cleanup = MatchCleanup.noop();
        MatchOpenRequest request = new MatchOpenRequest(
                "duel",
                MatchPolicy.competitiveDefaults(),
                callbacks,
                cleanup,
                Set.of()
        );

        assertTrue(request.initialPlayers().isEmpty());
        assertEquals("duel", request.matchKey());
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType(), () -> type.getSimpleName() + "#" + methodName + " return type");
    }
}
