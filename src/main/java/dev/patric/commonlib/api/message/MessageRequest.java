package dev.patric.commonlib.api.message;

import java.util.Locale;
import java.util.Map;

/**
 * Message rendering request.
 *
 * @param key message key.
 * @param locale locale.
 * @param placeholders placeholder map.
 * @param count count used for pluralization.
 * @param contextTags additional context tags.
 */
public record MessageRequest(
        String key,
        Locale locale,
        Map<String, String> placeholders,
        Long count,
        Map<String, String> contextTags
) {

    /**
     * Creates a request without plural/context metadata.
     *
     * @param key key.
     * @param locale locale.
     * @param placeholders placeholders.
     * @return request.
     */
    public static MessageRequest simple(String key, Locale locale, Map<String, String> placeholders) {
        return new MessageRequest(key, locale, placeholders, null, Map.of());
    }
}
