package dev.patric.commonlib.api.dialog;

import java.util.Objects;

/**
 * Notice dialog type specification.
 *
 * @param action notice action button.
 */
public record NoticeTypeSpec(DialogButtonSpec action) implements DialogTypeSpec {

    /**
     * Compact constructor validation.
     */
    public NoticeTypeSpec {
        action = Objects.requireNonNull(action, "action");
    }
}
