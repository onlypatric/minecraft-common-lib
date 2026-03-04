package dev.patric.commonlib.api.dialog;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Open request payload for dialog sessions.
 *
 * @param playerId target player id.
 * @param template template to open.
 * @param timeoutTicks timeout in ticks, 0 disables timeout.
 * @param locale locale for future localization hooks.
 * @param placeholders placeholder map for future render stages.
 * @param callbacks callback hooks.
 */
public record DialogOpenRequest(
        UUID playerId,
        DialogTemplate template,
        long timeoutTicks,
        Locale locale,
        Map<String, String> placeholders,
        DialogCallbacks callbacks
) {

    /**
     * Compact constructor validation.
     */
    public DialogOpenRequest {
        playerId = Objects.requireNonNull(playerId, "playerId");
        template = Objects.requireNonNull(template, "template");
        if (timeoutTicks < 0L) {
            throw new IllegalArgumentException("timeoutTicks must be >= 0");
        }
        locale = locale == null ? Locale.ENGLISH : locale;
        placeholders = Map.copyOf(placeholders == null ? Map.of() : placeholders);
        callbacks = callbacks == null ? DialogCallbacks.noop() : callbacks;
    }
}
