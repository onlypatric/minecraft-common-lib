package dev.patric.commonlib.api;

import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.guard.PolicyHook;
import org.bukkit.event.Event;

/**
 * Central event router with composable policies.
 */
public interface EventRouter {

    /**
     * Registers policy for an event type.
     *
     * @param eventType event class.
     * @param policy policy hook.
     * @param <E> event type.
     */
    <E extends Event> void registerPolicy(Class<E> eventType, PolicyHook<E> policy);

    /**
     * Routes event through registered policies.
     *
     * @param event event instance.
     * @param <E> event type.
     * @return resulting decision.
     */
    <E extends Event> PolicyDecision route(E event);
}
