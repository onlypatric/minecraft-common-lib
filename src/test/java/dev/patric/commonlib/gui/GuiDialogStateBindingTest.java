package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogEventResult;
import dev.patric.commonlib.api.dialog.DialogResponse;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSubmission;
import dev.patric.commonlib.api.dialog.DialogSubmitEvent;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import net.kyori.adventure.text.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiDialogStateBindingTest {

    private ServerMock server;
    private TestPlugin plugin;
    private GuiSessionService gui;
    private DialogService dialogService;
    private DialogTemplateRegistry templateRegistry;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        gui = runtime.services().require(GuiSessionService.class);
        dialogService = runtime.services().require(DialogService.class);
        templateRegistry = runtime.services().require(DialogTemplateRegistry.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void dialogSubmitBindsResponseIntoGuiState() {
        Player player = server.addPlayer();
        templateRegistry.register(template("dialog.gui.bind", "name"));

        GuiDefinition definition = GuiDsl.chest("menu.dialog.bind", 6)
                .dialogInputSlot(
                        13,
                        GuiItemView.of("PAPER", "Input"),
                        "dialog.gui.bind",
                        List.of(new DialogResponseBinding("name", "playerName", true))
                )
                .build();

        GuiSession session = gui.open(definition, player.getUniqueId(), GuiOpenOptions.defaults());
        GuiInteractionResult click = gui.interact(new SlotClickEvent(
                session.sessionId(),
                session.state().revision(),
                13,
                ClickAction.LEFT,
                SlotTransferType.NONE
        ));
        assertEquals(GuiInteractionResult.APPLIED, click);

        dev.patric.commonlib.api.dialog.DialogSession dialogSession = dialogService.activeByPlayer(player.getUniqueId()).stream()
                .findFirst()
                .orElseThrow();

        DialogSubmission submission = new DialogSubmission(
                dialogSession.sessionId(),
                player.getUniqueId(),
                "submit",
                new FixedDialogResponse(Map.of("name", "Patric")),
                System.currentTimeMillis()
        );
        DialogEventResult submitResult = dialogService.publish(new DialogSubmitEvent(dialogSession.sessionId(), 0L, submission));
        assertEquals(DialogEventResult.APPLIED, submitResult);

        GuiSession updated = gui.find(session.sessionId()).orElseThrow();
        assertEquals("Patric", updated.state().data().get("playerName"));
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
                        List.of(new PlainMessageBodySpec(Component.text("Body"), 150)),
                        List.of(new TextInputSpec(inputKey, 100, Component.text("Name"), true, "", 32, null, null))
                ),
                new NoticeTypeSpec(new DialogButtonSpec(
                        Component.text("Submit"),
                        null,
                        120,
                        new CustomActionSpec("submit", Map.of())
                )),
                Map.of()
        );
    }

    private record FixedDialogResponse(Map<String, String> data) implements DialogResponse {
        @Override
        public Optional<String> text(String key) {
            return Optional.ofNullable(data.get(key));
        }

        @Override
        public Optional<Boolean> bool(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<Float> number(String key) {
            return Optional.empty();
        }

        @Override
        public String rawPayload() {
            return data.toString();
        }

        @Override
        public Map<String, Object> asMap() {
            return Map.copyOf(data);
        }
    }
}
