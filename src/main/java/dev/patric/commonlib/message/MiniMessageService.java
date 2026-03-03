package dev.patric.commonlib.message;

import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.api.MessageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * MiniMessage-backed message renderer with locale fallback.
 */
public final class MiniMessageService implements MessageService {

    private static final String DEFAULT_NAMESPACE = "default";

    private final ConfigService configService;
    private final String messagesPath;
    private final Locale defaultLocale;
    private final MiniMessage miniMessage;

    /**
     * Creates a message service backed by a YAML messages file.
     *
     * @param configService configuration service.
     * @param messagesPath relative path for messages config.
     * @param defaultLocale default locale.
     */
    public MiniMessageService(ConfigService configService, String messagesPath, Locale defaultLocale) {
        this.configService = Objects.requireNonNull(configService, "configService");
        this.messagesPath = Objects.requireNonNull(messagesPath, "messagesPath");
        this.defaultLocale = Objects.requireNonNull(defaultLocale, "defaultLocale");
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public Component render(String key, Map<String, String> placeholders, Locale locale) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(placeholders, "placeholders");
        Locale effectiveLocale = locale == null ? defaultLocale : locale;

        String template = resolveTemplate(key, effectiveLocale);
        TagResolver[] resolvers = placeholders.entrySet()
                .stream()
                .map(entry -> Placeholder.unparsed(entry.getKey(), entry.getValue()))
                .toArray(TagResolver[]::new);

        return miniMessage.deserialize(template, resolvers);
    }

    @Override
    public Component render(String key) {
        return render(key, Map.of(), defaultLocale);
    }

    private String resolveTemplate(String key, Locale locale) {
        FileConfiguration config = configService.load(messagesPath);
        for (String candidate : candidateKeys(key, locale)) {
            if (config.isString(candidate)) {
                return Objects.requireNonNull(config.getString(candidate));
            }
        }
        return "<red>[common-lib] Missing message key: " + key + "</red>";
    }

    private List<String> candidateKeys(String key, Locale locale) {
        List<String> keys = new ArrayList<>();

        String languageTag = locale.toLanguageTag();
        String language = locale.getLanguage();
        if (!languageTag.isBlank()) {
            keys.add(languageTag + "." + key);
        }
        if (!language.isBlank()) {
            keys.add(language + "." + key);
        }
        keys.add(DEFAULT_NAMESPACE + "." + key);
        keys.add(key);

        return keys;
    }
}
