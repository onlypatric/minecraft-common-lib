package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDefinitionRegistry;
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

class GuiSubMenuNavigationStackTest {

    private ServerMock server;
    private TestPlugin plugin;
    private GuiSessionService gui;
    private GuiDefinitionRegistry registry;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        gui = runtime.services().require(GuiSessionService.class);
        registry = runtime.services().require(GuiDefinitionRegistry.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void subMenuOpensAndBackReturnsToPreviousMenu() {
        Player player = server.addPlayer();

        GuiDefinition root = GuiDsl.chest("menu.root", 6)
                .subMenuSlot(11, GuiItemView.of("CHEST", "Sub"), "menu.sub")
                .build();
        GuiDefinition sub = GuiDsl.chest("menu.sub", 6)
                .backSlot(0, GuiItemView.of("ARROW", "Back"))
                .build();

        registry.register(root);
        registry.register(sub);

        GuiSession session = gui.open(root, player.getUniqueId(), GuiOpenOptions.defaults());
        GuiInteractionResult toSub = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                11,
                ClickAction.LEFT,
                SlotTransferType.NONE
        ));
        assertEquals(GuiInteractionResult.APPLIED, toSub);

        GuiSession inSub = gui.find(session.sessionId()).orElseThrow();
        assertEquals("menu.sub", inSub.viewKey());

        GuiInteractionResult back = gui.interact(new SlotClickEvent(
                inSub.sessionId(),
                inSub.state().revision(),
                0,
                ClickAction.LEFT,
                SlotTransferType.NONE
        ));
        assertEquals(GuiInteractionResult.APPLIED, back);

        GuiSession backInRoot = gui.find(session.sessionId()).orElseThrow();
        assertEquals("menu.root", backInRoot.viewKey());
    }
}
