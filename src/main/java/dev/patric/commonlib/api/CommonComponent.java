package dev.patric.commonlib.api;

/**
 * Lifecycle component managed by the common runtime.
 */
public interface CommonComponent {

    /**
     * Unique component id for logs and diagnostics.
     *
     * @return component id.
     */
    String id();

    /**
     * Called during plugin load phase.
     *
     * @param context runtime context.
     */
    default void onLoad(CommonContext context) {
        // default no-op
    }

    /**
     * Called during plugin enable phase.
     *
     * @param context runtime context.
     */
    default void onEnable(CommonContext context) {
        // default no-op
    }

    /**
     * Called during plugin disable phase.
     *
     * @param context runtime context.
     */
    default void onDisable(CommonContext context) {
        // default no-op
    }
}
