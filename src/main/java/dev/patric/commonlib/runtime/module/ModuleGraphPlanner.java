package dev.patric.commonlib.runtime.module;

import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModulePlanResult;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.api.module.ModuleStatus;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Plans module dependency graph and returns deterministic topological ordering.
 */
public final class ModuleGraphPlanner {

    /**
     * Planner result including module references and initial statuses.
     *
     * @param sortedModules runnable modules sorted topologically.
     * @param planResult API-facing planner result.
     */
    public record Plan(List<CommonModule> sortedModules, ModulePlanResult planResult) {

        /**
         * Copies internal collections to immutable snapshots.
         */
        public Plan {
            sortedModules = List.copyOf(sortedModules == null ? List.of() : sortedModules);
            planResult = Objects.requireNonNull(planResult, "planResult");
        }
    }

    /**
     * Builds dependency plan for declared modules.
     *
     * @param declaredModules module collection.
     * @return resolved planning result.
     */
    public Plan plan(Collection<? extends CommonModule> declaredModules) {
        Objects.requireNonNull(declaredModules, "declaredModules");

        Map<String, CommonModule> modulesById = indexModules(declaredModules);
        List<String> sortedIds = new ArrayList<>(modulesById.keySet());
        sortedIds.sort(String::compareTo);

        Map<String, ModuleStatus> statuses = new LinkedHashMap<>();
        for (String id : sortedIds) {
            CommonModule module = modulesById.get(id);
            statuses.put(id, new ModuleStatus(id, ModuleState.REGISTERED, "", module.dependsOn(), System.currentTimeMillis()));
        }

        markMissingDependencies(sortedIds, modulesById, statuses);
        Set<String> cycleNodes = detectCycles(sortedIds, modulesById, statuses);
        for (String node : cycleNodes) {
            ModuleStatus previous = statuses.get(node);
            statuses.put(
                    node,
                    previous.withState(ModuleState.SKIPPED_CYCLE, "cycle-detected", System.currentTimeMillis())
            );
        }

        List<String> runnableOrder = buildTopologicalOrder(sortedIds, modulesById, statuses);
        List<CommonModule> sortedModules = runnableOrder.stream().map(modulesById::get).toList();
        List<ModuleStatus> initialStatuses = sortedIds.stream().map(statuses::get).toList();
        ModulePlanResult planResult = new ModulePlanResult(runnableOrder, initialStatuses, !cycleNodes.isEmpty());
        return new Plan(sortedModules, planResult);
    }

    private static Map<String, CommonModule> indexModules(Collection<? extends CommonModule> modules) {
        Map<String, CommonModule> modulesById = new HashMap<>();
        for (CommonModule module : modules) {
            CommonModule nonNullModule = Objects.requireNonNull(module, "module");
            String id = normalizeId(nonNullModule.id());
            CommonModule previous = modulesById.putIfAbsent(id, nonNullModule);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate module id: " + id);
            }
        }
        return modulesById;
    }

    private static String normalizeId(String rawId) {
        String id = Objects.requireNonNull(rawId, "module id").trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Module id cannot be blank");
        }
        return id;
    }

    private static void markMissingDependencies(
            List<String> sortedIds,
            Map<String, CommonModule> modulesById,
            Map<String, ModuleStatus> statuses
    ) {
        for (String id : sortedIds) {
            CommonModule module = modulesById.get(id);
            Set<String> missing = new TreeSet<>();
            for (String dependencyId : module.dependsOn()) {
                if (!modulesById.containsKey(dependencyId)) {
                    missing.add(dependencyId);
                }
            }
            if (!missing.isEmpty()) {
                ModuleStatus previous = statuses.get(id);
                String reason = "missing-dependency:" + String.join(",", missing);
                statuses.put(
                        id,
                        previous.withState(ModuleState.SKIPPED_MISSING_DEPENDENCY, reason, System.currentTimeMillis())
                );
            }
        }
    }

    private static Set<String> detectCycles(
            List<String> sortedIds,
            Map<String, CommonModule> modulesById,
            Map<String, ModuleStatus> statuses
    ) {
        Map<String, Integer> visitState = new HashMap<>();
        Set<String> cycleNodes = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();

        for (String id : sortedIds) {
            if (visitState.getOrDefault(id, 0) != 0) {
                continue;
            }
            if (statuses.get(id).state() != ModuleState.REGISTERED) {
                continue;
            }
            dfsDetectCycle(id, modulesById, statuses, visitState, stack, cycleNodes);
        }

        return cycleNodes;
    }

    private static void dfsDetectCycle(
            String id,
            Map<String, CommonModule> modulesById,
            Map<String, ModuleStatus> statuses,
            Map<String, Integer> visitState,
            Deque<String> stack,
            Set<String> cycleNodes
    ) {
        visitState.put(id, 1);
        stack.push(id);

        List<String> dependencies = new ArrayList<>(modulesById.get(id).dependsOn());
        dependencies.sort(String::compareTo);

        for (String dependencyId : dependencies) {
            if (!modulesById.containsKey(dependencyId)) {
                continue;
            }
            if (statuses.get(dependencyId).state() != ModuleState.REGISTERED) {
                continue;
            }

            int depState = visitState.getOrDefault(dependencyId, 0);
            if (depState == 0) {
                dfsDetectCycle(dependencyId, modulesById, statuses, visitState, stack, cycleNodes);
            } else if (depState == 1) {
                markCyclePath(stack, dependencyId, cycleNodes);
            }
        }

        stack.pop();
        visitState.put(id, 2);
    }

    private static void markCyclePath(Deque<String> stack, String pivotId, Set<String> cycleNodes) {
        for (String item : stack) {
            cycleNodes.add(item);
            if (item.equals(pivotId)) {
                break;
            }
        }
    }

    private static List<String> buildTopologicalOrder(
            List<String> sortedIds,
            Map<String, CommonModule> modulesById,
            Map<String, ModuleStatus> statuses
    ) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> outgoing = new HashMap<>();

        for (String id : sortedIds) {
            if (statuses.get(id).state() != ModuleState.REGISTERED) {
                continue;
            }
            inDegree.put(id, 0);
            outgoing.put(id, new ArrayList<>());
        }

        for (String id : sortedIds) {
            if (statuses.get(id).state() != ModuleState.REGISTERED) {
                continue;
            }

            List<String> deps = new ArrayList<>(modulesById.get(id).dependsOn());
            deps.sort(Comparator.naturalOrder());
            for (String dependencyId : deps) {
                if (statuses.getOrDefault(
                        dependencyId,
                        new ModuleStatus("_", ModuleState.SKIPPED_MISSING_DEPENDENCY, "", Set.of(), 0)
                ).state() != ModuleState.REGISTERED) {
                    continue;
                }
                inDegree.compute(id, (ignored, value) -> value == null ? 1 : value + 1);
                outgoing.computeIfAbsent(dependencyId, ignored -> new ArrayList<>()).add(id);
            }
        }

        PriorityQueue<String> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> order = new ArrayList<>(inDegree.size());
        while (!queue.isEmpty()) {
            String current = queue.poll();
            order.add(current);

            List<String> dependents = outgoing.getOrDefault(current, Collections.emptyList());
            dependents.sort(String::compareTo);
            for (String dependent : dependents) {
                int next = inDegree.computeIfPresent(dependent, (ignored, value) -> value - 1);
                if (next == 0) {
                    queue.add(dependent);
                }
            }
        }

        return order;
    }
}
