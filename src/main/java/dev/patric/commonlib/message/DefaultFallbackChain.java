package dev.patric.commonlib.message;

import dev.patric.commonlib.api.message.FallbackChain;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Default locale fallback chain implementation.
 */
public final class DefaultFallbackChain implements FallbackChain {

    @Override
    public List<Locale> resolve(Locale requested, Locale defaultLocale) {
        Locale safeRequested = requested == null ? defaultLocale : requested;
        Locale safeDefault = Objects.requireNonNull(defaultLocale, "defaultLocale");

        LinkedHashSet<Locale> locales = new LinkedHashSet<>();
        locales.add(safeRequested);

        String language = safeRequested.getLanguage();
        if (!language.isBlank()) {
            locales.add(Locale.of(language));
        }

        locales.add(safeDefault);
        locales.add(Locale.ENGLISH);
        locales.add(Locale.ROOT);

        return new ArrayList<>(locales);
    }
}
