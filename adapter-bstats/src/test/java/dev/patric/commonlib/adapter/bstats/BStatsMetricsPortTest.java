package dev.patric.commonlib.adapter.bstats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BStatsMetricsPortTest {

    @Test
    void initializeRejectsInvalidInputsAndIsSafe() {
        BStatsMetricsPort port = new BStatsMetricsPort();

        assertFalse(port.initialize(null, 10));
        assertFalse(port.initialize(null, -1));
        assertFalse(port.addSimplePie("mode", () -> "solo"));
        assertFalse(port.addSingleLineChart("players", () -> 1));

        port.shutdown();
    }
}
