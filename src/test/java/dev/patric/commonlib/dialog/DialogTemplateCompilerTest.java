package dev.patric.commonlib.dialog;

import dev.patric.commonlib.api.dialog.CommandTemplateActionSpec;
import dev.patric.commonlib.api.dialog.ConfirmationTypeSpec;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTypeSpec;
import dev.patric.commonlib.api.dialog.NumberRangeInputSpec;
import dev.patric.commonlib.api.dialog.PlainMessageBodySpec;
import dev.patric.commonlib.api.dialog.StaticClickActionKind;
import dev.patric.commonlib.api.dialog.StaticClickActionSpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.runtime.dialog.DialogTemplateCompiler;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DialogTemplateCompilerTest {

    @Test
    void compilerBuildsDialogFromFullTemplateModel() {
        DialogTemplate template = new DialogTemplate(
                "dialog.compiler.confirm",
                new DialogBaseSpec(
                        Component.text("Confirm"),
                        Component.text("External"),
                        true,
                        false,
                        DialogAfterAction.WAIT_FOR_RESPONSE,
                        List.of(new PlainMessageBodySpec(Component.text("Are you sure?"), 180)),
                        List.of(
                                new TextInputSpec("reason", 180, Component.text("Reason"), true, "", 64, null, null),
                                new NumberRangeInputSpec("amount", 120, Component.text("Amount"), "%s", 0f, 10f, 5f, 1f)
                        )
                ),
                new ConfirmationTypeSpec(
                        new DialogButtonSpec(
                                Component.text("Yes"),
                                Component.text("Run"),
                                120,
                                new CommandTemplateActionSpec("/confirm $(reason)")
                        ),
                        new DialogButtonSpec(
                                Component.text("No"),
                                null,
                                120,
                                new StaticClickActionSpec(StaticClickActionKind.RUN_COMMAND, "/cancel")
                        )
                ),
                Map.of("source", "test")
        );

        DialogTemplateCompiler compiler = new DialogTemplateCompiler();
        Dialog dialog = compiler.compile(
                template,
                key -> Optional.of(template),
                (actionId, inputSpecs, additions) -> DialogAction.commandTemplate("/custom")
        );

        assertNotNull(dialog);
    }

    @Test
    void compilerSupportsCustomActionFactory() {
        DialogTemplate template = new DialogTemplate(
                "dialog.compiler.custom",
                new DialogBaseSpec(
                        Component.text("Custom"),
                        null,
                        true,
                        false,
                        DialogAfterAction.CLOSE,
                        List.of(),
                        List.of(new TextInputSpec("text", 160, Component.text("Text"), true, "", 32, null, null))
                ),
                (DialogTypeSpec) new ConfirmationTypeSpec(
                        new DialogButtonSpec(Component.text("OK"), null, 120, new CustomActionSpec("ok", Map.of())),
                        new DialogButtonSpec(Component.text("Cancel"), null, 120, new StaticClickActionSpec(StaticClickActionKind.SUGGEST_COMMAND, "/cancel"))
                ),
                Map.of()
        );

        DialogTemplateCompiler compiler = new DialogTemplateCompiler();
        Dialog dialog = compiler.compile(
                template,
                key -> Optional.of(template),
                (actionId, inputSpecs, additions) -> DialogAction.commandTemplate("/callback " + actionId)
        );

        assertNotNull(dialog);
    }
}
