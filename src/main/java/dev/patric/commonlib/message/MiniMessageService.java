package dev.patric.commonlib.message;

import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.api.MessageService;
import dev.patric.commonlib.api.message.FallbackChain;
import dev.patric.commonlib.api.message.MessageRequest;
import dev.patric.commonlib.api.message.PlaceholderResolver;
import dev.patric.commonlib.api.message.PluralRules;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;

/**
 * Legacy compatibility wrapper around {@link AdvancedMiniMessageService}.
 *
 * @deprecated use {@link AdvancedMiniMessageService}.
 */
@Deprecated(since = "0.3.0", forRemoval = false)
public final class MiniMessageService implements MessageService {

    private final AdvancedMiniMessageService delegate;

    /**
     * Creates the legacy wrapper using default fallback/plural strategies.
     *
     * @param configService configuration service.
     * @param messagesPath relative path for messages config.
     * @param defaultLocale default locale.
     */
    public MiniMessageService(ConfigService configService, String messagesPath, Locale defaultLocale) {
        this(configService, messagesPath, defaultLocale, new DefaultFallbackChain(), new DefaultPluralRules());
    }

    /**
     * Creates the legacy wrapper with explicit fallback/plural strategies.
     *
     * @param configService configuration service.
     * @param messagesPath relative path for messages config.
     * @param defaultLocale default locale.
     * @param fallbackChain fallback chain.
     * @param pluralRules plural rules.
     */
    public MiniMessageService(
            ConfigService configService,
            String messagesPath,
            Locale defaultLocale,
            FallbackChain fallbackChain,
            PluralRules pluralRules
    ) {
        this.delegate = new AdvancedMiniMessageService(
                Objects.requireNonNull(configService, "configService"),
                Objects.requireNonNull(messagesPath, "messagesPath"),
                Objects.requireNonNull(defaultLocale, "defaultLocale"),
                Objects.requireNonNull(fallbackChain, "fallbackChain"),
                Objects.requireNonNull(pluralRules, "pluralRules")
        );
    }

    @Override
    public Component render(MessageRequest request) {
        return delegate.render(request);
    }

    @Override
    public Component render(String key, Locale locale) {
        return delegate.render(key, locale);
    }

    @Override
    public Component render(String key, Map<String, String> placeholders, Locale locale) {
        return delegate.render(key, placeholders, locale);
    }

    @Override
    public void registerResolver(PlaceholderResolver resolver) {
        delegate.registerResolver(resolver);
    }

    @Override
    public void setFallbackChain(FallbackChain chain) {
        delegate.setFallbackChain(chain);
    }
}
