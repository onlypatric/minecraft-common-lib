package dev.patric.commonlib.message;

import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.api.MessageService;
import dev.patric.commonlib.api.message.FallbackChain;
import dev.patric.commonlib.api.message.MessageRequest;
import dev.patric.commonlib.api.message.PlaceholderResolver;
import dev.patric.commonlib.api.message.PluralRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Advanced MiniMessage-backed renderer with resolvers, fallback chain and plural support.
 */
public final class AdvancedMiniMessageService implements MessageService {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final Pattern PLACEHOLDER_TAG_PATTERN = Pattern.compile("<([a-zA-Z0-9_:-]+)>");

    private final ConfigService configService;
    private final String messagesPath;
    private final Locale defaultLocale;
    private final MiniMessage miniMessage;
    private final PluralRules pluralRules;
    private final List<PlaceholderResolver> resolvers;
    private volatile FallbackChain fallbackChain;

    /**
     * Creates a message service backed by a YAML messages file.
     *
     * @param configService configuration service.
     * @param messagesPath relative path for messages config.
     * @param defaultLocale default locale.
     * @param fallbackChain fallback strategy.
     * @param pluralRules plural rules strategy.
     */
    public AdvancedMiniMessageService(
            ConfigService configService,
            String messagesPath,
            Locale defaultLocale,
            FallbackChain fallbackChain,
            PluralRules pluralRules
    ) {
        this.configService = Objects.requireNonNull(configService, "configService");
        this.messagesPath = Objects.requireNonNull(messagesPath, "messagesPath");
        this.defaultLocale = Objects.requireNonNull(defaultLocale, "defaultLocale");
        this.fallbackChain = Objects.requireNonNull(fallbackChain, "fallbackChain");
        this.pluralRules = Objects.requireNonNull(pluralRules, "pluralRules");
        this.miniMessage = MiniMessage.miniMessage();
        this.resolvers = new CopyOnWriteArrayList<>();
    }

    @Override
    public Component render(MessageRequest request) {
        Objects.requireNonNull(request, "request");
        String key = Objects.requireNonNull(request.key(), "request.key");
        Locale locale = request.locale() == null ? defaultLocale : request.locale();

        String template = resolveTemplate(key, locale, request.count());
        Map<String, String> resolvedPlaceholders = resolvePlaceholders(template, request);
        TagResolver[] tagResolvers = resolvedPlaceholders.entrySet().stream()
                .map(entry -> Placeholder.unparsed(entry.getKey(), entry.getValue()))
                .toArray(TagResolver[]::new);

        return miniMessage.deserialize(template, tagResolvers);
    }

    @Override
    public Component render(String key, Locale locale) {
        return render(MessageRequest.simple(key, locale == null ? defaultLocale : locale, Map.of()));
    }

    @Override
    public Component render(String key, Map<String, String> placeholders, Locale locale) {
        return render(MessageRequest.simple(key, locale == null ? defaultLocale : locale, placeholders == null ? Map.of() : placeholders));
    }

    @Override
    public void registerResolver(PlaceholderResolver resolver) {
        resolvers.add(Objects.requireNonNull(resolver, "resolver"));
    }

    @Override
    public void setFallbackChain(FallbackChain chain) {
        this.fallbackChain = Objects.requireNonNull(chain, "chain");
    }

    private String resolveTemplate(String key, Locale locale, Long count) {
        FileConfiguration config = configService.load(messagesPath);
        for (String candidate : candidateKeys(key, locale, count)) {
            if (config.isString(candidate)) {
                return Objects.requireNonNull(config.getString(candidate));
            }
        }
        return "<red>[common-lib] Missing message key: " + key + "</red>";
    }

    private List<String> candidateKeys(String key, Locale locale, Long count) {
        List<String> keys = new ArrayList<>();
        List<Locale> locales = fallbackChain.resolve(locale, defaultLocale);

        String pluralForm = count == null ? null : pluralRules.selectForm(locale, count);
        for (Locale current : locales) {
            String localeTag = current.toLanguageTag();
            String language = current.getLanguage();
            if (!localeTag.isBlank()) {
                if (pluralForm != null) {
                    keys.add(localeTag + "." + key + "." + pluralForm);
                }
                keys.add(localeTag + "." + key);
            }
            if (!language.isBlank()) {
                if (pluralForm != null) {
                    keys.add(language + "." + key + "." + pluralForm);
                }
                keys.add(language + "." + key);
            }
        }

        if (pluralForm != null) {
            keys.add(DEFAULT_NAMESPACE + "." + key + "." + pluralForm);
        }
        keys.add(DEFAULT_NAMESPACE + "." + key);
        if (pluralForm != null) {
            keys.add(key + "." + pluralForm);
        }
        keys.add(key);

        return deduplicate(keys);
    }

    private static List<String> deduplicate(List<String> keys) {
        return new ArrayList<>(new LinkedHashSet<>(keys));
    }

    private Map<String, String> resolvePlaceholders(String template, MessageRequest request) {
        Map<String, String> values = new HashMap<>();
        if (request.placeholders() != null) {
            values.putAll(request.placeholders());
        }
        if (request.count() != null) {
            values.putIfAbsent("count", String.valueOf(request.count()));
        }
        if (request.contextTags() != null) {
            values.putAll(request.contextTags());
        }

        for (String placeholderKey : extractPlaceholderKeys(template)) {
            if (values.containsKey(placeholderKey)) {
                continue;
            }
            for (PlaceholderResolver resolver : resolvers) {
                if (resolver.supports(placeholderKey)) {
                    values.put(placeholderKey, resolver.resolve(placeholderKey, request));
                    break;
                }
            }
        }

        return values;
    }

    private static List<String> extractPlaceholderKeys(String template) {
        List<String> keys = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_TAG_PATTERN.matcher(template);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }
}
