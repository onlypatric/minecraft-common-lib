package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.port.CommandPort;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Command port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingCommandPort implements CommandPort {

    private final CommandPort fallback;
    private final AtomicReference<CommandPort> delegate;

    /**
     * Creates a delegating command port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingCommandPort(CommandPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(CommandPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public void register(CommandModel model) {
        delegate.get().register(model);
    }

    @Override
    public void unregister(String root) {
        delegate.get().unregister(root);
    }

    @Override
    public boolean supportsSuggestions() {
        return delegate.get().supportsSuggestions();
    }
}
