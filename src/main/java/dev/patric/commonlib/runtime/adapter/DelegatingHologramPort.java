package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.port.HologramPort;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;

/**
 * Hologram port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingHologramPort implements HologramPort {

    private final HologramPort fallback;
    private final AtomicReference<HologramPort> delegate;

    /**
     * Creates a delegating hologram port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingHologramPort(HologramPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(HologramPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public UUID create(String hologramKey, Location location, List<String> lines) {
        return delegate.get().create(hologramKey, location, lines);
    }

    @Override
    public boolean updateLines(UUID hologramId, List<String> lines) {
        return delegate.get().updateLines(hologramId, lines);
    }

    @Override
    public boolean move(UUID hologramId, Location location) {
        return delegate.get().move(hologramId, location);
    }

    @Override
    public boolean delete(UUID hologramId) {
        return delegate.get().delete(hologramId);
    }
}
