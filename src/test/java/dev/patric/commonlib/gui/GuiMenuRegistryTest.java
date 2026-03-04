package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.runtime.DefaultGuiDefinitionRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiMenuRegistryTest {

    @Test
    void registerFindAllAndUnregisterWork() {
        DefaultGuiDefinitionRegistry registry = new DefaultGuiDefinitionRegistry();
        GuiDefinition root = GuiDsl.chest("menu.root", 6).build();
        GuiDefinition sub = GuiDsl.chest("menu.sub", 6).build();

        registry.register(root);
        registry.register(sub);

        assertTrue(registry.find("menu.root").isPresent());
        assertTrue(registry.find("menu.sub").isPresent());
        assertEquals(2, registry.all().size());

        assertTrue(registry.unregister("menu.root"));
        assertFalse(registry.find("menu.root").isPresent());
        assertFalse(registry.unregister("menu.root"));
    }
}
