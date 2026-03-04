package dev.patric.commonlib.api.dialog;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Base dialog rendering specification.
 *
 * @param title title component.
 * @param externalTitle external button title.
 * @param canCloseWithEscape whether ESC closes dialog.
 * @param pause whether single-player pause is requested.
 * @param afterAction after-action behavior.
 * @param body body blocks.
 * @param inputs input blocks.
 */
public record DialogBaseSpec(
        Component title,
        @Nullable Component externalTitle,
        boolean canCloseWithEscape,
        boolean pause,
        DialogAfterAction afterAction,
        List<DialogBodySpec> body,
        List<DialogInputSpec> inputs
) {

    /**
     * Compact constructor validation.
     */
    public DialogBaseSpec {
        title = Objects.requireNonNull(title, "title");
        afterAction = Objects.requireNonNull(afterAction, "afterAction");
        body = List.copyOf(body == null ? List.of() : body);
        inputs = List.copyOf(inputs == null ? List.of() : inputs);
    }
}
