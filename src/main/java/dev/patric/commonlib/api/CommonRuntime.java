package dev.patric.commonlib.api;

import dev.patric.commonlib.runtime.CommonRuntimeBuilder;
import java.util.Collection;
import java.util.Locale;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main runtime contract orchestrating common library components.
 */
public interface CommonRuntime {

    /**
     * Runs load phase for registered components.
     */
    void onLoad();

    /**
     * Runs enable phase for registered components.
     */
    void onEnable();

    /**
     * Runs disable phase for registered components and scheduler resources.
     */
    void onDisable();

    /**
     * Accesses shared registry for runtime services.
     *
     * @return service registry.
     */
    ServiceRegistry services();

    /**
     * Creates a builder for a plugin runtime.
     *
     * @param plugin owning plugin.
     * @return builder instance.
     */
    static Builder builder(JavaPlugin plugin) {
        return new CommonRuntimeBuilder(plugin);
    }

    /**
     * Builder for runtime composition.
     */
    interface Builder {

        /**
         * Registers a runtime component.
         *
         * @param component component to add.
         * @return same builder.
         */
        Builder component(CommonComponent component);

        /**
         * Registers runtime components in order.
         *
         * @param components components to add.
         * @return same builder.
         */
        Builder components(Collection<? extends CommonComponent> components);

        /**
         * Sets path used for the main config file.
         *
         * @param mainConfigPath relative path.
         * @return same builder.
         */
        Builder mainConfigPath(String mainConfigPath);

        /**
         * Sets path used for messages config file.
         *
         * @param messagesConfigPath relative path.
         * @return same builder.
         */
        Builder messagesConfigPath(String messagesConfigPath);

        /**
         * Sets default locale for message rendering fallback.
         *
         * @param locale locale.
         * @return same builder.
         */
        Builder defaultLocale(Locale locale);

        /**
         * Enables or disables built-in core components.
         *
         * @param includeDefaults true to include default config and message services.
         * @return same builder.
         */
        Builder includeDefaultCoreComponents(boolean includeDefaults);

        /**
         * Builds runtime instance.
         *
         * @return immutable runtime.
         */
        CommonRuntime build();
    }
}
