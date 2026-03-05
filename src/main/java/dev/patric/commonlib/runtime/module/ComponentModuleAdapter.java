package dev.patric.commonlib.runtime.module;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.module.CommonModule;
import java.util.Objects;
import java.util.Set;

/**
 * Adapter turning legacy components into module declarations.
 */
public final class ComponentModuleAdapter implements CommonModule {

    private final CommonComponent delegate;
    private final Set<String> dependencies;

    /**
     * Creates a component-backed module with no dependencies.
     *
     * @param delegate legacy component.
     */
    public ComponentModuleAdapter(CommonComponent delegate) {
        this(delegate, Set.of());
    }

    /**
     * Creates a component-backed module with explicit dependencies.
     *
     * @param delegate legacy component.
     * @param dependencies module dependencies.
     */
    public ComponentModuleAdapter(CommonComponent delegate, Set<String> dependencies) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.dependencies = Set.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public Set<String> dependsOn() {
        return dependencies;
    }

    @Override
    public String description() {
        return "component-adapter:" + delegate.getClass().getSimpleName();
    }

    @Override
    public void onLoad(CommonContext ctx) {
        delegate.onLoad(ctx);
    }

    @Override
    public void onEnable(CommonContext ctx) {
        delegate.onEnable(ctx);
    }

    @Override
    public void onDisable(CommonContext ctx) {
        delegate.onDisable(ctx);
    }
}
