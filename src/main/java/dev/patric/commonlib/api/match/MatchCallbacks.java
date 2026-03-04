package dev.patric.commonlib.api.match;

/**
 * Optional match callbacks for state lifecycle hooks.
 */
public interface MatchCallbacks {

    /**
     * Called when a state is entered.
     */
    default void onStateEnter(MatchSession session) {
    }

    /**
     * Called every engine tick while a state is active.
     */
    default void onStateTick(MatchSession session, long stateTick) {
    }

    /**
     * Called when leaving the current state.
     */
    default void onStateExit(MatchSession session) {
    }

    /**
     * Called once when the match reaches terminal close.
     */
    default void onEnd(MatchSession session, EndReason reason) {
    }
}
