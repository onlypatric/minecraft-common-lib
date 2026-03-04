package dev.patric.commonlib.api.dialog;

import java.util.Map;
import java.util.Optional;

/**
 * Typed access to dialog response payload.
 */
public interface DialogResponse {

    /**
     * Reads text value by key.
     *
     * @param key response key.
     * @return optional value.
     */
    Optional<String> text(String key);

    /**
     * Reads boolean value by key.
     *
     * @param key response key.
     * @return optional value.
     */
    Optional<Boolean> bool(String key);

    /**
     * Reads numeric value by key.
     *
     * @param key response key.
     * @return optional value.
     */
    Optional<Float> number(String key);

    /**
     * Returns raw binary payload serialized as SNBT.
     *
     * @return raw payload string.
     */
    String rawPayload();

    /**
     * Returns best-effort parsed map representation.
     *
     * @return parsed payload map.
     */
    Map<String, Object> asMap();
}
