package dev.patric.commonlib.api.bootstrap;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.error.ErrorCodes;
import dev.patric.commonlib.api.error.OperationError;
import dev.patric.commonlib.api.error.OperationResult;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bootstrap helpers for plugin host classes integrating CommonRuntime.
 */
public final class RuntimeBootstrap {

    private RuntimeBootstrap() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Builds a runtime with optional customizer.
     *
     * @param plugin owning plugin.
     * @param customizer builder customizer.
     * @return operation result containing runtime or error.
     */
    public static OperationResult<CommonRuntime> build(JavaPlugin plugin, Consumer<CommonRuntime.Builder> customizer) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(customizer, "customizer");

        try {
            CommonRuntime.Builder builder = CommonRuntime.builder(plugin);
            customizer.accept(builder);
            return OperationResult.success(builder.build());
        } catch (RuntimeException ex) {
            return OperationResult.failure(OperationError.of(ErrorCodes.INTERNAL_ERROR, "Unable to build runtime", ex));
        }
    }

    /**
     * Builds a runtime with provided components.
     *
     * @param plugin owning plugin.
     * @param components components.
     * @return operation result containing runtime or error.
     */
    public static OperationResult<CommonRuntime> build(JavaPlugin plugin, CommonComponent... components) {
        return build(plugin, builder -> builder.components(Arrays.asList(components)));
    }

    /**
     * Executes runtime onLoad safely.
     *
     * @param runtime runtime instance.
     * @return operation result.
     */
    public static OperationResult<Void> safeLoad(CommonRuntime runtime) {
        return safeLifecycle("onLoad", runtime::onLoad);
    }

    /**
     * Executes runtime onEnable safely.
     *
     * @param runtime runtime instance.
     * @return operation result.
     */
    public static OperationResult<Void> safeEnable(CommonRuntime runtime) {
        return safeLifecycle("onEnable", runtime::onEnable);
    }

    /**
     * Executes runtime onDisable safely.
     *
     * @param runtime runtime instance.
     * @return operation result.
     */
    public static OperationResult<Void> safeDisable(CommonRuntime runtime) {
        return safeLifecycle("onDisable", runtime::onDisable);
    }

    private static OperationResult<Void> safeLifecycle(String phase, Runnable action) {
        Objects.requireNonNull(action, "action");
        try {
            action.run();
            return OperationResult.success(null);
        } catch (RuntimeException ex) {
            return OperationResult.failure(OperationError.of(ErrorCodes.ILLEGAL_STATE, "Lifecycle phase failed: " + phase, ex));
        }
    }
}
