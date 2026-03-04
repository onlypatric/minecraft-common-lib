package dev.patric.commonlib.api.match;

/**
 * Rejoin and session timeout policy.
 *
 * @param enabled whether rejoin flow is enabled.
 * @param rejoinWindowTicks max ticks to accept reconnect.
 * @param sessionTimeoutTicks max ticks to keep disconnected session data.
 */
public record RejoinPolicy(boolean enabled, long rejoinWindowTicks, long sessionTimeoutTicks) {

    /**
     * Creates a rejoin policy.
     */
    public RejoinPolicy {
        if (rejoinWindowTicks < 0L) {
            throw new IllegalArgumentException("rejoinWindowTicks must be >= 0");
        }
        if (sessionTimeoutTicks < 0L) {
            throw new IllegalArgumentException("sessionTimeoutTicks must be >= 0");
        }
    }

    /**
     * Competitive defaults for match-based servers.
     */
    public static RejoinPolicy competitiveDefaults() {
        return new RejoinPolicy(true, 200L, 1200L);
    }
}
