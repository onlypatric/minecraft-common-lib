package dev.patric.commonlib.runtime.module;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModulePlanResult;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.api.module.ModuleStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates module lifecycle with soft-disable semantics.
 */
public final class ModuleLifecycleOrchestrator {

    private final CommonContext context;
    private final RuntimeLogger logger;
    private final DefaultModuleRegistry moduleRegistry;
    private final boolean diagnosticsEnabled;
    private final List<String> enabledOrder;
    private final Map<String, CommonModule> modulesById;
    private List<CommonModule> orderedModules;

    /**
     * Creates a module orchestrator.
     *
     * @param context runtime context.
     * @param logger runtime logger.
     * @param moduleRegistry shared module registry.
     * @param diagnosticsEnabled true to emit transition diagnostics.
     */
    public ModuleLifecycleOrchestrator(
            CommonContext context,
            RuntimeLogger logger,
            DefaultModuleRegistry moduleRegistry,
            boolean diagnosticsEnabled
    ) {
        this.context = Objects.requireNonNull(context, "context");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.moduleRegistry = Objects.requireNonNull(moduleRegistry, "moduleRegistry");
        this.diagnosticsEnabled = diagnosticsEnabled;
        this.enabledOrder = new ArrayList<>();
        this.modulesById = new LinkedHashMap<>();
        this.orderedModules = List.of();
    }

    /**
     * Plans and registers module statuses.
     *
     * @param modules declared modules.
     * @return planner result.
     */
    public ModulePlanResult initialize(Collection<? extends CommonModule> modules) {
        ModuleGraphPlanner.Plan plan = new ModuleGraphPlanner().plan(modules);
        ModulePlanResult planResult = plan.planResult();

        moduleRegistry.registerInitialStatuses(planResult.initialStatuses());
        orderedModules = plan.sortedModules();
        modulesById.clear();
        for (CommonModule module : orderedModules) {
            modulesById.put(module.id(), module);
        }

        if (diagnosticsEnabled) {
            logger.info("module-plan:order=" + String.join(",", planResult.sortedOrder()));
        }
        return planResult;
    }

    /**
     * Runs onLoad phase for modules in deterministic order.
     */
    public void onLoad() {
        for (CommonModule module : orderedModules) {
            String moduleId = module.id();
            ModuleStatus status = moduleRegistry.find(moduleId).orElse(null);
            if (status == null || status.state() != ModuleState.REGISTERED) {
                continue;
            }

            Optional<String> inactiveDependency = firstInactiveDependency(module, true);
            if (inactiveDependency.isPresent()) {
                updateStatus(moduleId, ModuleState.SKIPPED_DEPENDENCY_INACTIVE, inactiveDependency.get());
                continue;
            }

            try {
                module.onLoad(context);
            } catch (RuntimeException ex) {
                updateStatus(moduleId, ModuleState.FAILED_LOAD, "load-failed:" + ex.getClass().getSimpleName());
                logger.error("module load failed: " + moduleId, ex);
            }
        }
    }

    /**
     * Runs onEnable phase for modules in deterministic order.
     */
    public void onEnable() {
        for (CommonModule module : orderedModules) {
            String moduleId = module.id();
            ModuleStatus status = moduleRegistry.find(moduleId).orElse(null);
            if (status == null || status.state() != ModuleState.REGISTERED) {
                continue;
            }

            Optional<String> inactiveDependency = firstInactiveDependency(module, false);
            if (inactiveDependency.isPresent()) {
                updateStatus(moduleId, ModuleState.SKIPPED_DEPENDENCY_INACTIVE, inactiveDependency.get());
                continue;
            }

            try {
                module.onEnable(context);
                updateStatus(moduleId, ModuleState.ENABLED, "");
                enabledOrder.add(moduleId);
            } catch (RuntimeException ex) {
                updateStatus(moduleId, ModuleState.FAILED_ENABLE, "enable-failed:" + ex.getClass().getSimpleName());
                logger.error("module enable failed: " + moduleId, ex);
            }
        }
    }

    /**
     * Runs onDisable phase in reverse enabled order.
     */
    public void onDisable() {
        for (int i = enabledOrder.size() - 1; i >= 0; i--) {
            String moduleId = enabledOrder.get(i);
            CommonModule module = modulesById.get(moduleId);
            if (module == null) {
                continue;
            }

            ModuleStatus status = moduleRegistry.find(moduleId).orElse(null);
            if (status == null || status.state() != ModuleState.ENABLED) {
                continue;
            }

            try {
                module.onDisable(context);
            } catch (RuntimeException ex) {
                logger.error("module disable failed: " + moduleId, ex);
            } finally {
                updateStatus(moduleId, ModuleState.DISABLED, "");
            }
        }
        enabledOrder.clear();
    }

    private Optional<String> firstInactiveDependency(CommonModule module, boolean loadPhase) {
        List<String> dependencies = new ArrayList<>(module.dependsOn());
        dependencies.sort(String::compareTo);

        for (String dependencyId : dependencies) {
            ModuleStatus dependencyStatus = moduleRegistry.find(dependencyId).orElse(null);
            if (dependencyStatus == null) {
                return Optional.of("dependency-inactive:" + dependencyId);
            }

            if (loadPhase) {
                if (dependencyStatus.state() != ModuleState.REGISTERED && dependencyStatus.state() != ModuleState.ENABLED) {
                    return Optional.of("dependency-inactive:" + dependencyId);
                }
            } else if (dependencyStatus.state() != ModuleState.ENABLED) {
                return Optional.of("dependency-inactive:" + dependencyId);
            }
        }

        return Optional.empty();
    }

    private void updateStatus(String moduleId, ModuleState state, String reason) {
        moduleRegistry.updateState(moduleId, state, reason);
        if (diagnosticsEnabled) {
            String normalizedReason = reason == null || reason.isBlank() ? "none" : reason;
            logger.info("module:" + moduleId + ":" + state + ":" + normalizedReason);
        }
    }
}
