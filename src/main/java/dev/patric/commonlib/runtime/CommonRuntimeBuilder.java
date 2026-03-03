package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Default runtime builder.
 */
public final class CommonRuntimeBuilder implements CommonRuntime.Builder {

    private final JavaPlugin plugin;
    private final List<CommonComponent> components = new ArrayList<>();
    private String mainConfigPath = "config.yml";
    private String messagesConfigPath = "messages.yml";
    private Locale defaultLocale = Locale.ENGLISH;
    private boolean includeDefaultCoreComponents = true;

    /**
     * Creates a runtime builder bound to a plugin.
     *
     * @param plugin owning plugin.
     */
    public CommonRuntimeBuilder(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public CommonRuntime.Builder component(CommonComponent component) {
        components.add(Objects.requireNonNull(component, "component"));
        return this;
    }

    @Override
    public CommonRuntime.Builder components(Collection<? extends CommonComponent> components) {
        Objects.requireNonNull(components, "components");
        for (CommonComponent component : components) {
            component(component);
        }
        return this;
    }

    @Override
    public CommonRuntime.Builder mainConfigPath(String mainConfigPath) {
        this.mainConfigPath = Objects.requireNonNull(mainConfigPath, "mainConfigPath");
        return this;
    }

    @Override
    public CommonRuntime.Builder messagesConfigPath(String messagesConfigPath) {
        this.messagesConfigPath = Objects.requireNonNull(messagesConfigPath, "messagesConfigPath");
        return this;
    }

    @Override
    public CommonRuntime.Builder defaultLocale(Locale locale) {
        this.defaultLocale = Objects.requireNonNull(locale, "locale");
        return this;
    }

    @Override
    public CommonRuntime.Builder includeDefaultCoreComponents(boolean includeDefaults) {
        this.includeDefaultCoreComponents = includeDefaults;
        return this;
    }

    @Override
    public CommonRuntime build() {
        return new DefaultCommonRuntime(
                plugin,
                List.copyOf(components),
                mainConfigPath,
                messagesConfigPath,
                defaultLocale,
                includeDefaultCoreComponents
        );
    }
}
