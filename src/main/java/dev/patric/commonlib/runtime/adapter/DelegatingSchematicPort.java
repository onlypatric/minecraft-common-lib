package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;

/**
 * Schematic port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingSchematicPort implements SchematicPort {

    private final SchematicPort fallback;
    private final AtomicReference<SchematicPort> delegate;

    /**
     * Creates a delegating schematic port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingSchematicPort(SchematicPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(SchematicPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
        return delegate.get().paste(schematicKey, origin, options);
    }

    @Override
    public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
        return delegate.get().resetRegion(regionKey, templateKey, options);
    }
}
