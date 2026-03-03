package dev.patric.commonlib.lifecycle;

import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.guard.PolicyHook;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Simple in-memory event router evaluating registered policy hooks.
 */
public final class SimpleEventRouter implements EventRouter {

    private final Map<Class<? extends Event>, List<PolicyHook<? extends Event>>> policies = new ConcurrentHashMap<>();

    /**
     * Creates an empty event router.
     */
    public SimpleEventRouter() {
        // default constructor for explicit API documentation.
    }

    @Override
    public <E extends Event> void registerPolicy(Class<E> eventType, PolicyHook<E> policy) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(policy, "policy");
        policies.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(policy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> PolicyDecision route(E event) {
        Objects.requireNonNull(event, "event");

        for (Map.Entry<Class<? extends Event>, List<PolicyHook<? extends Event>>> entry : policies.entrySet()) {
            if (!entry.getKey().isAssignableFrom(event.getClass())) {
                continue;
            }

            for (PolicyHook<? extends Event> rawHook : entry.getValue()) {
                PolicyHook<E> hook = (PolicyHook<E>) rawHook;
                PolicyDecision decision = hook.evaluate(event);
                if (decision.denied()) {
                    if (event instanceof Cancellable cancellable) {
                        cancellable.setCancelled(true);
                    }
                    return decision;
                }
            }
        }

        return PolicyDecision.allow();
    }
}
