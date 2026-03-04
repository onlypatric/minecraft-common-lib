package dev.patric.commonlib.lifecycle;

import dev.patric.commonlib.guard.PolicyDecision;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventRouterOverheadBenchmarkTest {

    @Test
    void benchmarkShapeIsDeterministicAndCounterBased() {
        SimpleEventRouter router = new SimpleEventRouter();
        AtomicInteger inspected = new AtomicInteger();

        router.registerPolicy(BenchmarkEvent.class, event -> {
            inspected.incrementAndGet();
            if (event.sequence() % 50 == 0) {
                return PolicyDecision.deny("rate-limit");
            }
            return PolicyDecision.allow();
        });

        int total = 5_000;
        int denied = 0;
        for (int i = 1; i <= total; i++) {
            PolicyDecision decision = router.route(new BenchmarkEvent(i));
            if (decision.denied()) {
                denied++;
            }
        }

        assertEquals(total, inspected.get());
        assertEquals(total / 50, denied);
        assertTrue(denied < total);
    }

    private static final class BenchmarkEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();
        private final int sequence;

        private BenchmarkEvent(int sequence) {
            this.sequence = sequence;
        }

        private int sequence() {
            return sequence;
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
