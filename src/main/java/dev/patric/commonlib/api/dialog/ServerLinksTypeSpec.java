package dev.patric.commonlib.api.dialog;

import org.jspecify.annotations.Nullable;

/**
 * Server-links type specification.
 *
 * @param exitAction optional exit action button.
 * @param columns columns (>0).
 * @param buttonWidth button width in range [1,1024].
 */
public record ServerLinksTypeSpec(
        @Nullable DialogButtonSpec exitAction,
        int columns,
        int buttonWidth
) implements DialogTypeSpec {

    /**
     * Compact constructor validation.
     */
    public ServerLinksTypeSpec {
        if (columns <= 0) {
            throw new IllegalArgumentException("columns must be > 0");
        }
        if (buttonWidth < 1 || buttonWidth > 1024) {
            throw new IllegalArgumentException("buttonWidth must be in range [1,1024]");
        }
    }
}
