package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.noop.NoopMetricsPort;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MetricsPortContractTest {

    @Test
    void noopMetricsPortProvidesDeterministicSafeDefaults() {
        MetricsPort port = new NoopMetricsPort();
        JavaPlugin plugin = mock(JavaPlugin.class);

        assertTrue(port.initialize(plugin, 123));

        AtomicInteger calls = new AtomicInteger();
        assertTrue(port.addSimplePie("mode", () -> {
            calls.incrementAndGet();
            return "solo";
        }));
        assertTrue(port.addSingleLineChart("players", () -> {
            calls.incrementAndGet();
            return 10;
        }));
        port.shutdown();

        // no-op should not eagerly execute suppliers.
        assertFalse(calls.get() > 0);
    }
}
