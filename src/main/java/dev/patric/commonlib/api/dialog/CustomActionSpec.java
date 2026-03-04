package dev.patric.commonlib.api.dialog;

import java.util.Map;
import java.util.Objects;

/**
 * Custom callback action specification.
 *
 * @param actionId logical action id used by runtime callback dispatch.
 * @param additions optional user-defined metadata.
 */
public record CustomActionSpec(String actionId, Map<String, String> additions) implements DialogActionSpec {

    /**
     * Compact constructor validation.
     */
    public CustomActionSpec {
        actionId = Objects.requireNonNull(actionId, "actionId").trim();
        if (actionId.isEmpty()) {
            throw new IllegalArgumentException("actionId must not be blank");
        }
        additions = Map.copyOf(additions == null ? Map.of() : additions);
    }
}
