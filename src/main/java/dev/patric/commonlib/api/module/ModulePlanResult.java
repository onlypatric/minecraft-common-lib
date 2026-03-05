package dev.patric.commonlib.api.module;

import java.util.List;

/**
 * Planner output for declared modules.
 *
 * @param sortedOrder topologically sorted module ids considered runnable.
 * @param initialStatuses initial planner statuses for all declared modules.
 * @param hasCycles true when at least one cycle was detected.
 */
public record ModulePlanResult(
        List<String> sortedOrder,
        List<ModuleStatus> initialStatuses,
        boolean hasCycles
) {

    /**
     * Defensively copies result collections.
     */
    public ModulePlanResult {
        sortedOrder = List.copyOf(sortedOrder == null ? List.of() : sortedOrder);
        initialStatuses = List.copyOf(initialStatuses == null ? List.of() : initialStatuses);
    }
}
