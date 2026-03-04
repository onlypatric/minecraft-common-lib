package dev.patric.commonlib.api.match;

import dev.patric.commonlib.api.ServiceRegistry;

/**
 * Cleanup contract executed when a match closes.
 */
@FunctionalInterface
public interface MatchCleanup {

    /**
     * Runs close cleanup for a match.
     */
    void cleanup(MatchSession session, EndReason reason, ServiceRegistry services);

    /**
     * No-op cleanup implementation.
     */
    static MatchCleanup noop() {
        return (session, reason, services) -> {
        };
    }
}
