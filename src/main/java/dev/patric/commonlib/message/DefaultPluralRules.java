package dev.patric.commonlib.message;

import dev.patric.commonlib.api.message.PluralRules;
import java.util.Locale;

/**
 * Baseline plural rules with one/other forms.
 */
public final class DefaultPluralRules implements PluralRules {

    @Override
    public String selectForm(Locale locale, long count) {
        if (count == 1L) {
            return "one";
        }
        return "other";
    }
}
