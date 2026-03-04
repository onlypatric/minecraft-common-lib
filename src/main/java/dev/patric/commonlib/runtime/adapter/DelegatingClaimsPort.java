package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.port.ClaimsPort;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;

/**
 * Claims port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingClaimsPort implements ClaimsPort {

    private final ClaimsPort fallback;
    private final AtomicReference<ClaimsPort> delegate;

    /**
     * Creates a delegating claims port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingClaimsPort(ClaimsPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(ClaimsPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public boolean isInsideClaim(UUID playerId, Location location) {
        return delegate.get().isInsideClaim(playerId, location);
    }

    @Override
    public Optional<String> claimIdAt(Location location) {
        return delegate.get().claimIdAt(location);
    }

    @Override
    public boolean hasBuildPermission(UUID playerId, String claimId) {
        return delegate.get().hasBuildPermission(playerId, claimId);
    }

    @Override
    public boolean hasCombatPermission(UUID playerId, String claimId) {
        return delegate.get().hasCombatPermission(playerId, claimId);
    }
}
