package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.api.team.TeamService;
import java.util.Objects;

/**
 * Match callback helpers for arena/team/persistence foundation modules.
 */
public final class MatchFoundationHooks {

    private MatchFoundationHooks() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Wraps callbacks adding automatic team roster cleanup on match end.
     *
     * @param delegate callback delegate.
     * @param teamService team service.
     * @return wrapped callbacks.
     */
    public static MatchCallbacks withTeamCleanup(MatchCallbacks delegate, TeamService teamService) {
        Objects.requireNonNull(delegate, "delegate");
        Objects.requireNonNull(teamService, "teamService");

        return new MatchCallbacks() {
            @Override
            public void onStateEnter(MatchSession session) {
                delegate.onStateEnter(session);
            }

            @Override
            public void onStateTick(MatchSession session, long stateTick) {
                delegate.onStateTick(session, stateTick);
            }

            @Override
            public void onStateExit(MatchSession session) {
                delegate.onStateExit(session);
            }

            @Override
            public void onEnd(MatchSession session, EndReason reason) {
                try {
                    delegate.onEnd(session, reason);
                } finally {
                    teamService.clearRoster(session.matchId());
                }
            }
        };
    }
}
