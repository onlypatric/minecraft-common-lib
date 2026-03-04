package dev.patric.commonlib.api.match;

/**
 * Timer policy for countdown, running timeout and ending duration.
 *
 * @param countdownTicks ticks spent in countdown before running.
 * @param runningTimeoutTicks ticks before forcing TIME_LIMIT end (0 disables).
 * @param endingTicks ticks spent in ending before reset/cleanup.
 */
public record MatchTimingPolicy(long countdownTicks, long runningTimeoutTicks, long endingTicks) {

    /**
     * Creates a timing policy.
     */
    public MatchTimingPolicy {
        if (countdownTicks < 0L) {
            throw new IllegalArgumentException("countdownTicks must be >= 0");
        }
        if (runningTimeoutTicks < 0L) {
            throw new IllegalArgumentException("runningTimeoutTicks must be >= 0");
        }
        if (endingTicks < 0L) {
            throw new IllegalArgumentException("endingTicks must be >= 0");
        }
    }

    /**
     * Competitive defaults for quick adoption.
     */
    public static MatchTimingPolicy competitiveDefaults() {
        return new MatchTimingPolicy(100L, 0L, 40L);
    }
}
