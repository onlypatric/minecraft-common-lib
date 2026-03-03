package dev.patric.commonlib.api.message;

import java.util.Locale;

/**
 * Locale-aware plural rule selection.
 */
public interface PluralRules {

    /**
     * Selects plural form key.
     *
     * @param locale locale.
     * @param count count.
     * @return plural form key (e.g. one/other).
     */
    String selectForm(Locale locale, long count);
}
