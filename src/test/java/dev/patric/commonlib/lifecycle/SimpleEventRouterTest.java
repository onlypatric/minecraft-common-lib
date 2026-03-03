package dev.patric.commonlib.lifecycle;

import dev.patric.commonlib.guard.PolicyDecision;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleEventRouterTest {

    @Test
    void denyDecisionCancelsCancellableEvent() {
        SimpleEventRouter router = new SimpleEventRouter();
        router.registerPolicy(TestEvent.class, event -> PolicyDecision.deny("blocked"));

        TestEvent event = new TestEvent();
        PolicyDecision decision = router.route(event);

        assertTrue(decision.denied());
        assertEquals("blocked", decision.reason().orElseThrow());
        assertTrue(event.isCancelled());
    }

    public static final class TestEvent extends Event implements Cancellable {

        private static final HandlerList HANDLERS = new HandlerList();
        private boolean cancelled;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
