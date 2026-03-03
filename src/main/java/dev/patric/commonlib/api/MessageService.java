package dev.patric.commonlib.api;

import dev.patric.commonlib.api.message.FallbackChain;
import dev.patric.commonlib.api.message.MessageRequest;
import dev.patric.commonlib.api.message.PlaceholderResolver;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;

/**
 * Renders localized Adventure components from message configuration.
 */
public interface MessageService {

    /**
     * Renders a message request.
     *
     * @param request request payload.
     * @return rendered component.
     */
    Component render(MessageRequest request);

    /**
     * Renders a localized message with empty placeholders.
     *
     * @param key message key.
     * @param locale locale.
     * @return rendered component.
     */
    Component render(String key, Locale locale);

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
     * Registers a custom placeholder resolver.
     *
     * @param resolver resolver.
     */
    void registerResolver(PlaceholderResolver resolver);

    /**
     * Sets locale fallback chain strategy.
     *
     * @param chain fallback chain.
     */
    void setFallbackChain(FallbackChain chain);
}
