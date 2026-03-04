package dev.patric.commonlib.api.gui;

import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiApiV2ContractTest {

    @Test
    void v2CoreTypesAndMethodsArePresent() throws Exception {
        assertMethod(GuiSessionService.class, "open", GuiSession.class, GuiDefinition.class, UUID.class, GuiOpenOptions.class);
        assertMethod(GuiSessionService.class, "interact", GuiInteractionResult.class, GuiInteractionEvent.class);
        assertMethod(GuiDefinitionRegistry.class, "register", void.class, GuiDefinition.class);
        assertMethod(GuiDefinitionRegistry.class, "find", java.util.Optional.class, String.class);

        assertMethod(GuiPort.class, "open", boolean.class, GuiRenderModel.class);
        assertMethod(GuiPort.class, "render", boolean.class, UUID.class, GuiRenderPatch.class);
        assertMethod(GuiPort.class, "supports", boolean.class, GuiPortFeature.class);

        assertEquals(SlotInteractionPolicy.INPUT_DIALOG, SlotInteractionPolicy.valueOf("INPUT_DIALOG"));
        assertEquals(GuiType.CHEST_9X6, GuiType.valueOf("CHEST_9X6"));
        assertEquals(GuiInteractionResult.INVALID_ACTION, GuiInteractionResult.valueOf("INVALID_ACTION"));
        assertEquals(ToggleStateAction.class, Class.forName("dev.patric.commonlib.api.gui.ToggleStateAction"));
        assertEquals(OpenSubMenuAction.class, Class.forName("dev.patric.commonlib.api.gui.OpenSubMenuAction"));
        assertEquals(BackMenuAction.class, Class.forName("dev.patric.commonlib.api.gui.BackMenuAction"));
        assertEquals(DialogResponseBinding.class, Class.forName("dev.patric.commonlib.api.gui.DialogResponseBinding"));
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType());
    }
}
