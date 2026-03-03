package dev.patric.commonlib.api;

import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;

/**
 * Renders localized Adventure components from message configuration.
 */
public interface MessageService {

    /**
     * Renders a localized message with placeholders.
     *
     * @param key message key.
     * @param placeholders placeholder map.
     * @param locale locale.
     * @return rendered component.
     */
    Component render(String key, Map<String, String> placeholders, Locale locale);

    /**
     * Renders a message with default locale and empty placeholders.
     *
     * @param key message key.
     * @return rendered component.
     */
    Component render(String key);
}
