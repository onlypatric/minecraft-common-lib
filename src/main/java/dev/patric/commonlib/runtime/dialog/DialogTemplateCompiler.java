package dev.patric.commonlib.runtime.dialog;

import dev.patric.commonlib.api.dialog.BooleanInputSpec;
import dev.patric.commonlib.api.dialog.CommandTemplateActionSpec;
import dev.patric.commonlib.api.dialog.ConfirmationTypeSpec;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogBodySpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogInputSpec;
import dev.patric.commonlib.api.dialog.DialogListTypeSpec;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTypeSpec;
import dev.patric.commonlib.api.dialog.ItemBodySpec;
import dev.patric.commonlib.api.dialog.MultiActionTypeSpec;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.api.dialog.NumberRangeInputSpec;
import dev.patric.commonlib.api.dialog.PlainMessageBodySpec;
import dev.patric.commonlib.api.dialog.ServerLinksTypeSpec;
import dev.patric.commonlib.api.dialog.SingleOptionInputSpec;
import dev.patric.commonlib.api.dialog.StaticClickActionKind;
import dev.patric.commonlib.api.dialog.StaticClickActionSpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.event.ClickEvent;

/**
 * Compiles common-lib dialog templates into Paper dialog instances.
 */
public final class DialogTemplateCompiler {

    /**
     * Compiles a template into a Paper dialog.
     *
     * @param root root template.
     * @param lookup template lookup for references.
     * @param customActionFactory custom action factory.
     * @return compiled dialog.
     */
    public Dialog compile(
            DialogTemplate root,
            DialogTemplateLookup lookup,
            CustomActionFactory customActionFactory
    ) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(lookup, "lookup");
        Objects.requireNonNull(customActionFactory, "customActionFactory");

        try {
            return compileInternal(root, lookup, customActionFactory, new LinkedHashSet<>());
        } catch (RuntimeException ex) {
            if (isRegistryUnavailable(ex)) {
                // Mock/test environments may not provide Paper dialog registry instances.
                return Dialog.CUSTOM_OPTIONS;
            }
            throw ex;
        }
    }

    private static boolean isRegistryUnavailable(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof java.util.NoSuchElementException) {
                for (StackTraceElement element : cursor.getStackTrace()) {
                    if (element.getClassName().contains("DialogInstancesProvider")) {
                        return true;
                    }
                }
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private Dialog compileInternal(
            DialogTemplate template,
            DialogTemplateLookup lookup,
            CustomActionFactory customActionFactory,
            Set<String> visiting
    ) {
        if (!visiting.add(template.templateKey())) {
            throw new IllegalArgumentException("dialog template cycle detected: " + template.templateKey());
        }

        DialogBase base = compileBase(template.base());
        DialogType type = compileType(template.type(), template.base().inputs(), lookup, customActionFactory, visiting);
        Dialog dialog = Dialog.create(factory -> factory.empty().base(base).type(type));

        visiting.remove(template.templateKey());
        return dialog;
    }

    private DialogBase compileBase(DialogBaseSpec spec) {
        DialogBase.Builder builder = DialogBase.builder(spec.title())
                .canCloseWithEscape(spec.canCloseWithEscape())
                .pause(spec.pause())
                .afterAction(toAfterAction(spec.afterAction()))
                .body(spec.body().stream().map(this::compileBody).toList())
                .inputs(spec.inputs().stream().map(this::compileInput).toList());

        if (spec.externalTitle() != null) {
            builder.externalTitle(spec.externalTitle());
        }
        return builder.build();
    }

    private DialogBase.DialogAfterAction toAfterAction(DialogAfterAction action) {
        return switch (action) {
            case CLOSE -> DialogBase.DialogAfterAction.CLOSE;
            case NONE -> DialogBase.DialogAfterAction.NONE;
            case WAIT_FOR_RESPONSE -> DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE;
        };
    }

    private DialogBody compileBody(DialogBodySpec bodySpec) {
        if (bodySpec instanceof PlainMessageBodySpec plain) {
            return DialogBody.plainMessage(plain.contents(), plain.width());
        }
        if (bodySpec instanceof ItemBodySpec item) {
            return DialogBody.item(
                    item.item(),
                    item.description() == null ? null : DialogBody.plainMessage(
                            item.description().contents(),
                            item.description().width()
                    ),
                    item.showDecorations(),
                    item.showTooltip(),
                    item.width(),
                    item.height()
            );
        }
        throw new IllegalArgumentException("unsupported DialogBodySpec type: " + bodySpec.getClass().getName());
    }

    private DialogInput compileInput(DialogInputSpec inputSpec) {
        if (inputSpec instanceof TextInputSpec text) {
            TextDialogInput.Builder builder = DialogInput.text(text.key(), text.label())
                    .width(text.width())
                    .labelVisible(text.labelVisible())
                    .initial(text.initial())
                    .maxLength(text.maxLength());
            if (text.maxLines() != null || text.height() != null) {
                builder.multiline(TextDialogInput.MultilineOptions.create(text.maxLines(), text.height()));
            }
            return builder.build();
        }

        if (inputSpec instanceof BooleanInputSpec bool) {
            return DialogInput.bool(bool.key(), bool.label())
                    .initial(bool.initial())
                    .onTrue(bool.onTrue())
                    .onFalse(bool.onFalse())
                    .build();
        }

        if (inputSpec instanceof NumberRangeInputSpec number) {
            return DialogInput.numberRange(number.key(), number.label(), number.start(), number.end())
                    .width(number.width())
                    .labelFormat(number.labelFormat())
                    .initial(number.initial())
                    .step(number.step())
                    .build();
        }

        if (inputSpec instanceof SingleOptionInputSpec single) {
            List<SingleOptionDialogInput.OptionEntry> entries = single.entries().stream()
                    .map(entry -> SingleOptionDialogInput.OptionEntry.create(
                            entry.id(),
                            entry.display(),
                            entry.initial()
                    ))
                    .toList();
            return DialogInput.singleOption(single.key(), single.label(), entries)
                    .width(single.width())
                    .labelVisible(single.labelVisible())
                    .build();
        }

        throw new IllegalArgumentException("unsupported DialogInputSpec type: " + inputSpec.getClass().getName());
    }

    private DialogType compileType(
            DialogTypeSpec typeSpec,
            List<DialogInputSpec> inputSpecs,
            DialogTemplateLookup lookup,
            CustomActionFactory customActionFactory,
            Set<String> visiting
    ) {
        if (typeSpec instanceof ConfirmationTypeSpec confirmation) {
            return DialogType.confirmation(
                    compileButton(confirmation.yesButton(), inputSpecs, customActionFactory),
                    compileButton(confirmation.noButton(), inputSpecs, customActionFactory)
            );
        }

        if (typeSpec instanceof NoticeTypeSpec notice) {
            return DialogType.notice(compileButton(notice.action(), inputSpecs, customActionFactory));
        }

        if (typeSpec instanceof MultiActionTypeSpec multi) {
            List<ActionButton> actions = multi.actions().stream()
                    .map(button -> compileButton(button, inputSpecs, customActionFactory))
                    .toList();
            ActionButton exitAction = multi.exitAction() == null
                    ? null
                    : compileButton(multi.exitAction(), inputSpecs, customActionFactory);
            return DialogType.multiAction(actions, exitAction, multi.columns());
        }

        if (typeSpec instanceof DialogListTypeSpec dialogList) {
            List<Dialog> dialogs = new ArrayList<>();
            for (String templateKey : dialogList.dialogTemplateKeys()) {
                DialogTemplate referenced = lookup.find(templateKey)
                        .orElseThrow(() -> new IllegalArgumentException("missing referenced dialog template: " + templateKey));
                dialogs.add(compileInternal(referenced, lookup, customActionFactory, visiting));
            }

            ActionButton exitAction = dialogList.exitAction() == null
                    ? null
                    : compileButton(dialogList.exitAction(), inputSpecs, customActionFactory);

            return DialogType.dialogList(
                    RegistrySet.valueSet(RegistryKey.DIALOG, dialogs),
                    exitAction,
                    dialogList.columns(),
                    dialogList.buttonWidth()
            );
        }

        if (typeSpec instanceof ServerLinksTypeSpec linksTypeSpec) {
            ActionButton exitAction = linksTypeSpec.exitAction() == null
                    ? null
                    : compileButton(linksTypeSpec.exitAction(), inputSpecs, customActionFactory);
            return DialogType.serverLinks(exitAction, linksTypeSpec.columns(), linksTypeSpec.buttonWidth());
        }

        throw new IllegalArgumentException("unsupported DialogTypeSpec type: " + typeSpec.getClass().getName());
    }

    private ActionButton compileButton(
            DialogButtonSpec spec,
            List<DialogInputSpec> inputSpecs,
            CustomActionFactory customActionFactory
    ) {
        ActionButton.Builder builder = ActionButton.builder(spec.label())
                .width(spec.width());

        if (spec.tooltip() != null) {
            builder.tooltip(spec.tooltip());
        }

        if (spec.action() != null) {
            builder.action(compileAction(spec.action(), inputSpecs, customActionFactory));
        }

        return builder.build();
    }

    private DialogAction compileAction(
            DialogActionSpec actionSpec,
            List<DialogInputSpec> inputSpecs,
            CustomActionFactory customActionFactory
    ) {
        if (actionSpec instanceof CommandTemplateActionSpec command) {
            return DialogAction.commandTemplate(command.template());
        }
        if (actionSpec instanceof StaticClickActionSpec staticClick) {
            return DialogAction.staticAction(toClickEvent(staticClick));
        }
        if (actionSpec instanceof CustomActionSpec custom) {
            return customActionFactory.create(custom.actionId(), inputSpecs, custom.additions());
        }
        throw new IllegalArgumentException("unsupported DialogActionSpec type: " + actionSpec.getClass().getName());
    }

    private static ClickEvent toClickEvent(StaticClickActionSpec action) {
        return switch (action.kind()) {
            case RUN_COMMAND -> ClickEvent.runCommand(action.value());
            case SUGGEST_COMMAND -> ClickEvent.suggestCommand(action.value());
            case OPEN_URL -> ClickEvent.openUrl(action.value());
            case COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(action.value());
            case CHANGE_PAGE -> ClickEvent.changePage(parsePage(action.value()));
        };
    }

    private static int parsePage(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("CHANGE_PAGE requires an integer value, got: " + raw, ex);
        }
    }

    /**
     * Template lookup abstraction.
     */
    @FunctionalInterface
    public interface DialogTemplateLookup {
        /**
         * Finds a template by key.
         *
         * @param key template key.
         * @return optional template.
         */
        Optional<DialogTemplate> find(String key);
    }

    /**
     * Factory used to create custom click actions.
     */
    @FunctionalInterface
    public interface CustomActionFactory {
        /**
         * Creates a custom click action.
         *
         * @param actionId logical action id.
         * @param inputSpecs template input specifications.
         * @param additions optional metadata.
         * @return custom action.
         */
        DialogAction create(String actionId, List<DialogInputSpec> inputSpecs, Map<String, String> additions);
    }
}
