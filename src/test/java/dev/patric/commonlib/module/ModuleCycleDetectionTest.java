package dev.patric.commonlib.module;

import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.runtime.module.ModuleGraphPlanner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleCycleDetectionTest {

    @Test
    void cycleNodesAreSkippedAndReported() {
        ModuleGraphPlanner.Plan plan = new ModuleGraphPlanner().plan(List.of(
                new TestModule("a", Set.of("b")),
                new TestModule("b", Set.of("a")),
                new TestModule("independent", Set.of())
        ));

        Map<String, ModuleState> states = plan.planResult().initialStatuses().stream()
                .collect(java.util.stream.Collectors.toMap(status -> status.id(), status -> status.state()));

        assertTrue(plan.planResult().hasCycles());
        assertEquals(ModuleState.SKIPPED_CYCLE, states.get("a"));
        assertEquals(ModuleState.SKIPPED_CYCLE, states.get("b"));
        assertEquals(ModuleState.REGISTERED, states.get("independent"));
        assertEquals(List.of("independent"), plan.planResult().sortedOrder());
    }

    private record TestModule(String id, Set<String> dependsOn) implements CommonModule {
    }
}
