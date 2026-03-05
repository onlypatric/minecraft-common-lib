package dev.patric.commonlib.api.module;

import dev.patric.commonlib.api.CommonContext;
import java.util.Set;

/**
 * Lifecycle module managed by the common runtime module system.
 */
public interface CommonModule {

    /**
     * Unique module id.
     *
     * @return module id.
     */
    String id();

    /**
     * Required module ids.
     *
     * @return dependency ids.
     */
    default Set<String> dependsOn() {
        return Set.of();
    }

    /**
     * Optional module description used for diagnostics.
     *
     * @return human-readable description.
     */
    default String description() {
        return "";
    }

    /**
     * Called during plugin load phase.
     *
     * @param ctx runtime context.
     */
    default void onLoad(CommonContext ctx) {
        // default no-op
    }

    /**
     * Called during plugin enable phase.
     *
     * @param ctx runtime context.
     */
    default void onEnable(CommonContext ctx) {
        // default no-op
    }

    /**
     * Called during plugin disable phase.
     *
     * @param ctx runtime context.
     */
    default void onDisable(CommonContext ctx) {
        // default no-op
    }
}
