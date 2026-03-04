package dev.patric.commonlib.api.dialog;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Dialog-list type specification.
 *
 * @param dialogTemplateKeys referenced dialog template keys.
 * @param exitAction optional exit action button.
 * @param columns columns (>0).
 * @param buttonWidth button width in range [1,1024].
 */
public record DialogListTypeSpec(
        List<String> dialogTemplateKeys,
        @Nullable DialogButtonSpec exitAction,
        int columns,
        int buttonWidth
) implements DialogTypeSpec {

    /**
     * Compact constructor validation.
     */
    public DialogListTypeSpec {
        dialogTemplateKeys = List.copyOf(Objects.requireNonNull(dialogTemplateKeys, "dialogTemplateKeys")).stream()
                .map(key -> Objects.requireNonNull(key, "dialogTemplateKey").trim())
                .toList();
        if (dialogTemplateKeys.isEmpty()) {
            throw new IllegalArgumentException("dialogTemplateKeys must not be empty");
        }
        if (dialogTemplateKeys.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("dialogTemplateKeys must not contain blank values");
        }
        if (columns <= 0) {
            throw new IllegalArgumentException("columns must be > 0");
        }
        if (buttonWidth < 1 || buttonWidth > 1024) {
            throw new IllegalArgumentException("buttonWidth must be in range [1,1024]");
        }
    }
}
