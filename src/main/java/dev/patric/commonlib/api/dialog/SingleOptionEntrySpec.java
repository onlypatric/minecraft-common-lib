package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Radio option entry specification.
 *
 * @param id option id.
 * @param display optional display component.
 * @param initial whether selected by default.
 */
public record SingleOptionEntrySpec(String id, @Nullable Component display, boolean initial) {

    /**
     * Compact constructor validation.
     */
    public SingleOptionEntrySpec {
        id = Objects.requireNonNull(id, "id").trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }
}
