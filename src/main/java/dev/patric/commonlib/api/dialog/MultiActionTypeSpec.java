package dev.patric.commonlib.api.dialog;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Multi-action dialog type specification.
 *
 * @param actions action buttons.
 * @param exitAction optional exit action button.
 * @param columns number of columns (>0).
 */
public record MultiActionTypeSpec(
        List<DialogButtonSpec> actions,
        @Nullable DialogButtonSpec exitAction,
        int columns
) implements DialogTypeSpec {

    /**
     * Compact constructor validation.
     */
    public MultiActionTypeSpec {
        actions = List.copyOf(Objects.requireNonNull(actions, "actions"));
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("actions must not be empty");
        }
        if (columns <= 0) {
            throw new IllegalArgumentException("columns must be > 0");
        }
    }
}
