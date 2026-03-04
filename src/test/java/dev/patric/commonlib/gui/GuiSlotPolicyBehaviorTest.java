package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.api.dialog.PlainMessageBodySpec;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.DialogOpenOptionsMapping;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiInteractionResult;
import dev.patric.commonlib.api.gui.GuiOpenOptions;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.OpenDialogAction;
import dev.patric.commonlib.api.gui.SlotClickEvent;
import dev.patric.commonlib.api.gui.SlotInteractionPolicy;
import dev.patric.commonlib.api.gui.SlotTransferType;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiSlotPolicyBehaviorTest {

    private ServerMock server;
    private TestPlugin plugin;
    private CommonRuntime runtime;
    private GuiSessionService gui;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        gui = runtime.services().require(GuiSessionService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void buttonOnlyDeniesTakeTransfer() {
        Player player = server.addPlayer();

        GuiDefinition definition = GuiDsl.chest("menu.button", 6)
                .slot(13, slot -> slot.interaction(SlotInteractionPolicy.BUTTON_ONLY))
                .build();

        GuiSession session = gui.open(definition, player.getUniqueId(), GuiOpenOptions.defaults());
        GuiInteractionResult result = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                13,
                ClickAction.LEFT,
                SlotTransferType.TAKE
        ));

        assertEquals(GuiInteractionResult.DENIED_BY_POLICY, result);
    }

    @Test
    void inputDialogActionAppliesWhenTemplateExists() {
        Player player = server.addPlayer();
        DialogTemplateRegistry templates = runtime.services().require(DialogTemplateRegistry.class);
        templates.register(simpleTemplate("dialog.gui.input"));

        GuiDefinition definition = GuiDsl.chest("menu.dialog", 6)
                .slot(13, slot -> slot
                        .interaction(SlotInteractionPolicy.INPUT_DIALOG)
                        .action(new OpenDialogAction("dialog.gui.input", DialogOpenOptionsMapping.defaults())))
                .build();

        GuiSession session = gui.open(definition, player.getUniqueId(), new GuiOpenOptions(0L, true, true, Locale.ENGLISH, Map.of()));
        GuiInteractionResult result = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                13,
                ClickAction.LEFT,
                SlotTransferType.NONE
        ));

        assertEquals(GuiInteractionResult.APPLIED, result);
    }

    @Test
    void depositOnlyAllowsDepositAndDeniesTake() {
        Player player = server.addPlayer();

        GuiDefinition definition = GuiDsl.chest("menu.deposit", 6)
                .slot(10, slot -> slot.interaction(SlotInteractionPolicy.DEPOSIT_ONLY))
                .build();

        GuiSession session = gui.open(definition, player.getUniqueId(), GuiOpenOptions.defaults());

        GuiInteractionResult takeResult = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                10,
                ClickAction.LEFT,
                SlotTransferType.TAKE
        ));
        assertEquals(GuiInteractionResult.DENIED_BY_POLICY, takeResult);

        GuiSession refreshed = gui.find(session.sessionId()).orElseThrow();
        GuiInteractionResult depositResult = gui.interact(new SlotClickEvent(
                refreshed.sessionId(),
                refreshed.state().revision(),
                10,
                ClickAction.RIGHT,
                SlotTransferType.DEPOSIT
        ));
        assertEquals(GuiInteractionResult.APPLIED, depositResult);
    }

    private static DialogTemplate simpleTemplate(String key) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("Dialog"),
                        null,
                        true,
                        false,
                        DialogAfterAction.WAIT_FOR_RESPONSE,
                        java.util.List.of(new PlainMessageBodySpec(Component.text("Body"), 160)),
                        java.util.List.of()
                ),
                new NoticeTypeSpec(
                        new DialogButtonSpec(
                                Component.text("Submit"),
                                null,
                                120,
                                new CustomActionSpec("submit", Map.of())
                        )
                ),
                Map.of()
        );
    }
}
