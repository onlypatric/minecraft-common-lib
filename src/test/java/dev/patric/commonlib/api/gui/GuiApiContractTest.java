package dev.patric.commonlib.api.gui;

import dev.patric.commonlib.api.port.GuiPort;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiApiContractTest {

    @Test
    void guiSessionServiceContractMatchesExpectedSignatures() throws Exception {
        assertMethod(GuiSessionService.class, "open", GuiSession.class, GuiOpenRequest.class);
        assertMethod(GuiSessionService.class, "find", Optional.class, UUID.class);
        assertMethod(GuiSessionService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(GuiSessionService.class, "update", GuiUpdateResult.class, UUID.class, GuiState.class, long.class);
        assertMethod(GuiSessionService.class, "publish", GuiEventResult.class, GuiEvent.class);
        assertMethod(GuiSessionService.class, "close", boolean.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiSessionService.class, "closeAllByPlayer", int.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiSessionService.class, "closeAll", int.class, GuiCloseReason.class);
    }

    @Test
    void guiPortContractMatchesExpectedSignatures() throws Exception {
        assertMethod(GuiPort.class, "open", boolean.class, GuiSession.class);
        assertMethod(GuiPort.class, "render", boolean.class, UUID.class, GuiState.class);
        assertMethod(GuiPort.class, "close", boolean.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiPort.class, "supportsPortableEvents", boolean.class);
    }

    @Test
    void guiEnumsExposeExpectedConstants() {
        assertEquals(GuiSessionStatus.OPEN, GuiSessionStatus.valueOf("OPEN"));
        assertEquals(GuiCloseReason.TIMEOUT, GuiCloseReason.valueOf("TIMEOUT"));
        assertEquals(ClickAction.SHIFT_LEFT, ClickAction.valueOf("SHIFT_LEFT"));
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType());
    }
}
