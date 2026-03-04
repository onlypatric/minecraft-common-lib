package dev.patric.commonlib.api.dialog;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DialogApiContractTest {

    @Test
    void dialogServiceContractMatchesExpectedSignatures() throws Exception {
        assertMethod(DialogService.class, "open", DialogSession.class, DialogOpenRequest.class);
        assertMethod(DialogService.class, "find", Optional.class, UUID.class);
        assertMethod(DialogService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(DialogService.class, "publish", DialogEventResult.class, DialogEvent.class);
        assertMethod(DialogService.class, "close", boolean.class, UUID.class, DialogCloseReason.class);
        assertMethod(DialogService.class, "closeAllByPlayer", int.class, UUID.class, DialogCloseReason.class);
        assertMethod(DialogService.class, "closeAll", int.class, DialogCloseReason.class);
    }

    @Test
    void dialogTemplateRegistryContractMatchesExpectedSignatures() throws Exception {
        assertMethod(DialogTemplateRegistry.class, "register", void.class, DialogTemplate.class);
        assertMethod(DialogTemplateRegistry.class, "find", Optional.class, String.class);
        assertMethod(DialogTemplateRegistry.class, "all", List.class);
        assertMethod(DialogTemplateRegistry.class, "unregister", boolean.class, String.class);
    }

    @Test
    void dialogResponseContractMatchesExpectedSignatures() throws Exception {
        assertMethod(DialogResponse.class, "text", Optional.class, String.class);
        assertMethod(DialogResponse.class, "bool", Optional.class, String.class);
        assertMethod(DialogResponse.class, "number", Optional.class, String.class);
        assertMethod(DialogResponse.class, "rawPayload", String.class);
        assertMethod(DialogResponse.class, "asMap", Map.class);
    }

    @Test
    void enumsExposeExpectedConstants() {
        assertEquals(DialogSessionStatus.OPEN, DialogSessionStatus.valueOf("OPEN"));
        assertEquals(DialogCloseReason.SUBMITTED, DialogCloseReason.valueOf("SUBMITTED"));
        assertEquals(DialogEventResult.APPLIED, DialogEventResult.valueOf("APPLIED"));
        assertEquals(DialogAfterAction.WAIT_FOR_RESPONSE, DialogAfterAction.valueOf("WAIT_FOR_RESPONSE"));
        assertEquals(StaticClickActionKind.RUN_COMMAND, StaticClickActionKind.valueOf("RUN_COMMAND"));
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType());
    }
}
