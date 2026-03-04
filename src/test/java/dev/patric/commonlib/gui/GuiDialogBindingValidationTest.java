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
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.DialogResponseBinding;
import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDsl;
import dev.patric.commonlib.api.gui.GuiInteractionResult;
import dev.patric.commonlib.api.gui.GuiItemView;
import dev.patric.commonlib.api.gui.GuiOpenOptions;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.SlotClickEvent;
import dev.patric.commonlib.api.gui.SlotTransferType;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiDialogBindingValidationTest {

    private ServerMock server;
    private TestPlugin plugin;
    private GuiSessionService gui;
    private DialogTemplateRegistry templates;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        gui = runtime.services().require(GuiSessionService.class);
        templates = runtime.services().require(DialogTemplateRegistry.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void missingDialogInputKeyInBindingFailsAction() {
        Player player = server.addPlayer();
        templates.register(template("dialog.validation", "existing"));

        GuiDefinition definition = GuiDsl.chest("menu.validation", 6)
                .dialogInputSlot(
                        12,
                        GuiItemView.of("PAPER", "Input"),
                        "dialog.validation",
                        List.of(new DialogResponseBinding("missing", "target", true))
                )
                .build();

        GuiSession session = gui.open(definition, player.getUniqueId(), GuiOpenOptions.defaults());
        GuiInteractionResult result = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                12,
                ClickAction.LEFT,
                SlotTransferType.NONE
        ));

        assertEquals(GuiInteractionResult.INVALID_ACTION, result);
    }

    private static DialogTemplate template(String key, String inputKey) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("Dialog"),
                        null,
                        true,
                        false,
                        DialogAfterAction.WAIT_FOR_RESPONSE,
                        List.of(new PlainMessageBodySpec(Component.text("Body"), 120)),
                        List.of(new TextInputSpec(inputKey, 100, Component.text("Input"), true, "", 32, null, null))
                ),
                new NoticeTypeSpec(new DialogButtonSpec(
                        Component.text("Submit"),
                        null,
                        100,
                        new CustomActionSpec("submit", Map.of())
                )),
                Map.of()
        );
    }
}
