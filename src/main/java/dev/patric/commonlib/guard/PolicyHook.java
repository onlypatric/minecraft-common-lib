package dev.patric.commonlib.guard;

import org.bukkit.event.Event;

/**
 * Functional hook for policy checks.
 *
 * @param <E> event type.
 */
@FunctionalInterface
public interface PolicyHook<E extends Event> {

    /**
     * Evaluates policy for event.
     *
     * @param event event.
     * @return decision.
     */
    PolicyDecision evaluate(E event);
}
