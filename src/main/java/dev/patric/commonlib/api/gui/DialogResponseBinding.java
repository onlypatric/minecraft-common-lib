package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Binding rule that maps one dialog response key into one GUI state key.
 *
 * @param responseKey dialog response key.
 * @param stateKey target GUI state key.
 * @param required whether missing response value should fail the action pipeline.
 */
public record DialogResponseBinding(
        String responseKey,
        String stateKey,
        boolean required
) {

    /**
     * Compact constructor validation.
     */
    public DialogResponseBinding {
        responseKey = Objects.requireNonNull(responseKey, "responseKey").trim();
        stateKey = Objects.requireNonNull(stateKey, "stateKey").trim();
        if (responseKey.isEmpty()) {
            throw new IllegalArgumentException("responseKey must not be blank");
        }
        if (stateKey.isEmpty()) {
            throw new IllegalArgumentException("stateKey must not be blank");
        }
    }
}
