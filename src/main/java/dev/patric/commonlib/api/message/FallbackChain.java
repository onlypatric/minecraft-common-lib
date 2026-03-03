package dev.patric.commonlib.api.message;

import java.util.List;
import java.util.Locale;

/**
 * Locale fallback resolution strategy.
 */
public interface FallbackChain {

    /**
     * Resolves locale fallback chain.
     *
     * @param requested requested locale.
     * @param defaultLocale default locale.
     * @return ordered fallback locales.
     */
    List<Locale> resolve(Locale requested, Locale defaultLocale);
}
