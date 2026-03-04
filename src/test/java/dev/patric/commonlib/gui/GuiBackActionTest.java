package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiItemView;
import dev.patric.commonlib.api.gui.GuiInteractionResult;
import dev.patric.commonlib.api.gui.GuiOpenOptions;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.SlotClickEvent;
import dev.patric.commonlib.api.gui.SlotTransferType;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiBackActionTest {

    private ServerMock server;
    private TestPlugin plugin;
    private GuiSessionService gui;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        gui = runtime.services().require(GuiSessionService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void backActionWithoutStackReturnsInvalidAction() {
        Player player = server.addPlayer();
        GuiDefinition definition = GuiDsl.chest("menu.single", 6)
                .backSlot(0, GuiItemView.of("ARROW", "Back"))
                .build();

        GuiSession session = gui.open(definition, player.getUniqueId(), GuiOpenOptions.defaults());
        GuiInteractionResult result = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                0,
                ClickAction.LEFT,
                SlotTransferType.NONE
        ));
        assertEquals(GuiInteractionResult.INVALID_ACTION, result);
    }
}
