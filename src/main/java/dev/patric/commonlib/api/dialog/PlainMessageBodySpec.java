package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import net.kyori.adventure.text.Component;

/**
 * Plain text body block specification.
 *
 * @param contents body contents.
 * @param width body width in range [1,1024].
 */
public record PlainMessageBodySpec(Component contents, int width) implements DialogBodySpec {

    /**
     * Compact constructor validation.
     */
    public PlainMessageBodySpec {
        contents = Objects.requireNonNull(contents, "contents");
        if (width < 1 || width > 1024) {
            throw new IllegalArgumentException("width must be in range [1,1024]");
        }
    }
}
