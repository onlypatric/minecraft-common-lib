package dev.patric.commonlib.dialog;

import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogListTypeSpec;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.runtime.DefaultDialogTemplateRegistry;
import dev.patric.commonlib.runtime.dialog.DialogTemplateCompiler;
import dev.patric.commonlib.runtime.dialog.DialogTemplateValidator;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogListResolutionTest {

    @Test
    void validatorRejectsMissingReference() {
        DefaultDialogTemplateRegistry registry = new DefaultDialogTemplateRegistry();
        DialogTemplate root = listTemplate("dialog.list.root", List.of("dialog.list.missing"));

        DialogTemplateValidator validator = new DialogTemplateValidator();
        assertThrows(IllegalArgumentException.class, () -> validator.validate(root, registry));
    }

    @Test
    void validatorRejectsCycle() {
        DefaultDialogTemplateRegistry registry = new DefaultDialogTemplateRegistry();
        DialogTemplate root = listTemplate("dialog.cycle.root", List.of("dialog.cycle.child"));
        DialogTemplate child = listTemplate("dialog.cycle.child", List.of("dialog.cycle.root"));
        registry.register(child);

        DialogTemplateValidator validator = new DialogTemplateValidator();
        assertThrows(IllegalArgumentException.class, () -> validator.validate(root, registry));
    }

    @Test
    void compilerSupportsValidListReferences() {
        DefaultDialogTemplateRegistry registry = new DefaultDialogTemplateRegistry();
        DialogTemplate ref1 = noticeTemplate("dialog.list.ref1");
        DialogTemplate ref2 = noticeTemplate("dialog.list.ref2");
        registry.register(ref1);
        registry.register(ref2);

        DialogTemplate root = listTemplate("dialog.list.ok", List.of("dialog.list.ref1", "dialog.list.ref2"));
        DialogTemplateValidator validator = new DialogTemplateValidator();
        validator.validate(root, registry);

        DialogTemplateCompiler compiler = new DialogTemplateCompiler();
        assertDoesNotThrow(() -> compiler.compile(
                root,
                key -> key.equals(root.templateKey()) ? Optional.of(root) : registry.find(key),
                (actionId, inputSpecs, additions) -> DialogAction.commandTemplate("/noop")
        ));
    }

    private static DialogTemplate listTemplate(String key, List<String> refs) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("List"),
                        null,
                        true,
                        false,
                        DialogAfterAction.CLOSE,
                        List.of(),
                        List.of()
                ),
                new DialogListTypeSpec(refs, null, 2, 120),
                Map.of()
        );
    }

    private static DialogTemplate noticeTemplate(String key) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("Ref"),
                        null,
                        true,
                        false,
                        DialogAfterAction.CLOSE,
                        List.of(),
                        List.of()
                ),
                new NoticeTypeSpec(new DialogButtonSpec(Component.text("Ok"), null, 100, null)),
                Map.of()
        );
    }
}
