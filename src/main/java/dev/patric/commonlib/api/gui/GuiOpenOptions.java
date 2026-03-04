package dev.patric.commonlib.api.gui;

import java.util.Locale;
import java.util.Map;

/**
 * Options used when opening a GUI session.
 *
 * @param timeoutTicks session timeout in ticks.
 * @param replaceExisting whether existing player sessions are replaced.
 * @param closeOnPluginDisable whether session should be closed during runtime disable.
 * @param locale viewer locale.
 * @param placeholders open-time placeholders.
 */
public record GuiOpenOptions(
        long timeoutTicks,
        boolean replaceExisting,
        boolean closeOnPluginDisable,
        Locale locale,
        Map<String, String> placeholders
) {

    /**
     * Compact constructor validation.
     */
    public GuiOpenOptions {
        if (timeoutTicks < 0L) {
            throw new IllegalArgumentException("timeoutTicks must be >= 0");
        }
        locale = locale == null ? Locale.ENGLISH : locale;
        placeholders = Map.copyOf(placeholders == null ? Map.of() : placeholders);
    }

    /**
     * Returns sensible defaults.
     *
     * @return default options.
     */
    public static GuiOpenOptions defaults() {
        return new GuiOpenOptions(0L, true, true, Locale.ENGLISH, Map.of());
    }
}
