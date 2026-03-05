package dev.patric.commonlib.module;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.runtime.module.ModuleGraphPlanner;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleGraphPlannerTest {

    @Test
    void plannerOrdersModulesTopologicallyWithLexicographicTieBreak() {
        ModuleGraphPlanner.Plan plan = new ModuleGraphPlanner().plan(List.of(
                new TestModule("core", Set.of()),
                new TestModule("features-b", Set.of("core")),
                new TestModule("features-a", Set.of("core"))
        ));

        assertEquals(List.of("core", "features-a", "features-b"), plan.planResult().sortedOrder());
        assertFalse(plan.planResult().hasCycles());

        assertEquals(
                List.of(ModuleState.REGISTERED, ModuleState.REGISTERED, ModuleState.REGISTERED),
                plan.planResult().initialStatuses().stream().map(status -> status.state()).toList()
        );
    }

    @Test
    void plannerRejectsDuplicateModuleIds() {
        assertThrows(IllegalArgumentException.class, () -> new ModuleGraphPlanner().plan(List.of(
                new TestModule("dup", Set.of()),
                new TestModule("dup", Set.of())
        )));
    }

    @Test
    void plannerMarksMissingDependencies() {
        ModuleGraphPlanner.Plan plan = new ModuleGraphPlanner().plan(List.of(
                new TestModule("feature", Set.of("missing-core"))
        ));

        assertEquals(ModuleState.SKIPPED_MISSING_DEPENDENCY, plan.planResult().initialStatuses().getFirst().state());
        assertEquals(List.of(), plan.planResult().sortedOrder());
    }

    private record TestModule(String id, Set<String> dependsOn) implements CommonModule {

        @Override
        public void onLoad(CommonContext ctx) {
            // no-op
        }
    }
}
