package dev.patric.commonlib.runtime.module;

import dev.patric.commonlib.api.module.ModuleRegistry;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.api.module.ModuleStatus;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default in-memory module registry used by runtime orchestration.
 */
public final class DefaultModuleRegistry implements ModuleRegistry {

    private final Map<String, ModuleStatus> statuses;
    private final List<String> insertionOrder;

    /**
     * Creates an empty module registry.
     */
    public DefaultModuleRegistry() {
        this.statuses = new ConcurrentHashMap<>();
        this.insertionOrder = new CopyOnWriteArrayList<>();
    }

    /**
     * Replaces all statuses with planner output.
     *
     * @param initialStatuses planner statuses.
     */
    public void registerInitialStatuses(List<ModuleStatus> initialStatuses) {
        Objects.requireNonNull(initialStatuses, "initialStatuses");

        statuses.clear();
        insertionOrder.clear();
        for (ModuleStatus status : initialStatuses) {
            statuses.put(status.id(), status);
            insertionOrder.add(status.id());
        }
    }

    /**
     * Updates status for a module.
     *
     * @param moduleId module id.
     * @param state next state.
     * @param reason optional reason.
     */
    public void updateState(String moduleId, ModuleState state, String reason) {
        Objects.requireNonNull(moduleId, "moduleId");
        Objects.requireNonNull(state, "state");

        statuses.compute(moduleId, (id, previous) -> {
            ModuleStatus base = previous == null
                    ? new ModuleStatus(id, ModuleState.REGISTERED, "", Set.of(), System.currentTimeMillis())
                    : previous;
            return base.withState(state, reason, System.currentTimeMillis());
        });

        if (!insertionOrder.contains(moduleId)) {
            insertionOrder.add(moduleId);
        }
    }

    @Override
    public Optional<ModuleStatus> find(String moduleId) {
        Objects.requireNonNull(moduleId, "moduleId");
        return Optional.ofNullable(statuses.get(moduleId));
    }

    @Override
    public List<ModuleStatus> all() {
        Set<String> ids = new LinkedHashSet<>(insertionOrder);
        ids.addAll(statuses.keySet());

        List<ModuleStatus> ordered = new ArrayList<>(ids.size());
        for (String id : ids) {
            ModuleStatus status = statuses.get(id);
            if (status != null) {
                ordered.add(status);
            }
        }
        return List.copyOf(ordered);
    }

    @Override
    public boolean isEnabled(String moduleId) {
        return find(moduleId)
                .map(ModuleStatus::state)
                .filter(state -> state == ModuleState.ENABLED)
                .isPresent();
    }
}
