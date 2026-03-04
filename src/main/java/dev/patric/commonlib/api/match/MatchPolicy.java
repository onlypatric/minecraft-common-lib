package dev.patric.commonlib.api.match;

import java.util.Objects;

/**
 * Aggregates timing and rejoin behavior for a match.
 *
 * @param timing match timing policy.
 * @param rejoin rejoin/session timeout policy.
 */
public record MatchPolicy(MatchTimingPolicy timing, RejoinPolicy rejoin) {

    /**
     * Creates a match policy.
     */
    public MatchPolicy {
        timing = Objects.requireNonNull(timing, "timing");
        rejoin = Objects.requireNonNull(rejoin, "rejoin");
    }

    /**
     * Competitive defaults.
     */
    public static MatchPolicy competitiveDefaults() {
        return new MatchPolicy(MatchTimingPolicy.competitiveDefaults(), RejoinPolicy.competitiveDefaults());
    }
}
