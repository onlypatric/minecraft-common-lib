package dev.patric.commonlib.api.dialog;

import java.util.Objects;

/**
 * Confirmation dialog type specification.
 *
 * @param yesButton positive action button.
 * @param noButton negative action button.
 */
public record ConfirmationTypeSpec(DialogButtonSpec yesButton, DialogButtonSpec noButton) implements DialogTypeSpec {

    /**
     * Compact constructor validation.
     */
    public ConfirmationTypeSpec {
        yesButton = Objects.requireNonNull(yesButton, "yesButton");
        noButton = Objects.requireNonNull(noButton, "noButton");
    }
}
