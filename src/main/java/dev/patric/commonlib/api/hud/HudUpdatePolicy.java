package dev.patric.commonlib.api.hud;

/**
 * HUD update throttling and validation policy.
 *
 * @param minUpdateIntervalTicks minimum render interval per audience.
 * @param deduplicatePayload whether equal payload should be deduplicated.
 * @param maxScoreboardLines maximum allowed scoreboard lines.
 */
public record HudUpdatePolicy(long minUpdateIntervalTicks, boolean deduplicatePayload, int maxScoreboardLines) {

    /**
     * Compact constructor validation.
     */
    public HudUpdatePolicy {
        if (minUpdateIntervalTicks < 0L) {
            throw new IllegalArgumentException("minUpdateIntervalTicks must be >= 0");
        }
        if (maxScoreboardLines <= 0) {
            throw new IllegalArgumentException("maxScoreboardLines must be > 0");
        }
    }

    /**
     * Competitive defaults used by runtime services.
     *
     * @return default policy (5 ticks, dedupe enabled, 15 lines max).
     */
    public static HudUpdatePolicy competitiveDefaults() {
        return new HudUpdatePolicy(5L, true, 15);
    }
}
