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
        assertMethod(GuiSessionService.class, "open", GuiSession.class, GuiDefinition.class, UUID.class, GuiOpenOptions.class);
        assertMethod(GuiSessionService.class, "open", GuiSession.class, GuiOpenRequest.class);
        assertMethod(GuiSessionService.class, "find", Optional.class, UUID.class);
        assertMethod(GuiSessionService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(GuiSessionService.class, "update", GuiUpdateResult.class, UUID.class, GuiState.class, long.class);
        assertMethod(GuiSessionService.class, "interact", GuiInteractionResult.class, GuiInteractionEvent.class);
        assertMethod(GuiSessionService.class, "publish", GuiEventResult.class, GuiEvent.class);
        assertMethod(GuiSessionService.class, "close", boolean.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiSessionService.class, "closeAllByPlayer", int.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiSessionService.class, "closeAll", int.class, GuiCloseReason.class);
        assertMethod(GuiDefinitionRegistry.class, "register", void.class, GuiDefinition.class);
        assertMethod(GuiDefinitionRegistry.class, "find", Optional.class, String.class);
        assertMethod(GuiDefinitionRegistry.class, "all", List.class);
        assertMethod(GuiDefinitionRegistry.class, "unregister", boolean.class, String.class);
    }

    @Test
    void guiPortContractMatchesExpectedSignatures() throws Exception {
        assertMethod(GuiPort.class, "open", boolean.class, dev.patric.commonlib.api.gui.render.GuiRenderModel.class);
        assertMethod(GuiPort.class, "render", boolean.class, UUID.class, dev.patric.commonlib.api.gui.render.GuiRenderPatch.class);
        assertMethod(GuiPort.class, "close", boolean.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiPort.class, "supports", boolean.class, GuiPortFeature.class);
        assertMethod(GuiPort.class, "supportsPortableEvents", boolean.class);
    }

    @Test
    void guiEnumsExposeExpectedConstants() {
        assertEquals(GuiSessionStatus.OPEN, GuiSessionStatus.valueOf("OPEN"));
        assertEquals(GuiCloseReason.TIMEOUT, GuiCloseReason.valueOf("TIMEOUT"));
        assertEquals(ClickAction.SHIFT_LEFT, ClickAction.valueOf("SHIFT_LEFT"));
        assertEquals(SlotInteractionPolicy.TAKE_DEPOSIT, SlotInteractionPolicy.valueOf("TAKE_DEPOSIT"));
        assertEquals(GuiType.CHEST_9X6, GuiType.valueOf("CHEST_9X6"));
        assertEquals(ToggleStateAction.class, ToggleStateAction.class);
        assertEquals(OpenSubMenuAction.class, OpenSubMenuAction.class);
        assertEquals(BackMenuAction.class, BackMenuAction.class);
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType());
    }
}
