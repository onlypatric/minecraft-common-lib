package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiItemView;
import dev.patric.commonlib.api.gui.DialogResponseBinding;
import dev.patric.commonlib.api.gui.RunCommandAction;
import dev.patric.commonlib.api.gui.SlotInteractionPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GuiDslValidationTest {

    @Test
    void chestRowsOutsideRangeFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> GuiDsl.chest("menu.invalid", 0));
        assertThrows(IllegalArgumentException.class, () -> GuiDsl.chest("menu.invalid", 7));
    }

    @Test
    void slotOutsideLayoutFailsAtBuild() {
        assertThrows(IllegalArgumentException.class, () -> GuiDsl.chest("menu.bounds", 1)
                .slot(20, slot -> slot
                        .item(GuiItemView.of("STONE", "x"))
                        .interaction(SlotInteractionPolicy.BUTTON_ONLY))
                .build());
    }

    @Test
    void duplicateSlotFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> GuiDsl.chest("menu.duplicate", 1)
                .slot(0, slot -> slot.item(GuiItemView.of("STONE", "a")))
                .slot(0, slot -> slot.item(GuiItemView.of("STONE", "b"))));
    }

    @Test
    void helperMethodsCreateDefinition() {
        GuiDsl.chest("menu.helpers", 6)
                .button(10, GuiItemView.of("STONE", "buy"), List.of(new RunCommandAction("shop buy", true)))
                .inputDialogSlot(13, GuiItemView.of("PAPER", "input"), "dialog.key")
                .transferSlot(31, GuiItemView.of("CHEST", "storage"), SlotInteractionPolicy.TAKE_DEPOSIT)
                .build();
    }

    @Test
    void advancedHelpersCreateDefinition() {
        GuiDsl.chest("menu.advanced", 6)
                .switchSlot(11, "feature.enabled", GuiItemView.of("LIME_DYE", "on"), GuiItemView.of("GRAY_DYE", "off"), true)
                .subMenuSlot(15, GuiItemView.of("CHEST", "sub"), "menu.sub")
                .backSlot(45, GuiItemView.of("ARROW", "back"))
                .dialogInputSlot(
                        22,
                        GuiItemView.of("PAPER", "dialog"),
                        "dialog.input",
                        List.of(new DialogResponseBinding("name", "playerName", true))
                )
                .build();
    }
}
