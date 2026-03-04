package dev.patric.commonlib.api.dialog;

import java.util.Objects;

/**
 * Command-template action specification.
 *
 * @param template command template.
 */
public record CommandTemplateActionSpec(String template) implements DialogActionSpec {

    /**
     * Compact constructor validation.
     */
    public CommandTemplateActionSpec {
        template = Objects.requireNonNull(template, "template").trim();
        if (template.isEmpty()) {
            throw new IllegalArgumentException("template must not be blank");
        }
    }
}
