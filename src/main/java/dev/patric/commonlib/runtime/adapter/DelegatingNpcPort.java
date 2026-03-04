package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.port.NpcPort;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;

/**
 * NPC port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingNpcPort implements NpcPort {

    private final NpcPort fallback;
    private final AtomicReference<NpcPort> delegate;

    /**
     * Creates a delegating npc port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingNpcPort(NpcPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(NpcPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public UUID spawn(String templateKey, Location location, String displayName) {
        return delegate.get().spawn(templateKey, location, displayName);
    }

    @Override
    public boolean despawn(UUID npcId) {
        return delegate.get().despawn(npcId);
    }

    @Override
    public boolean updateDisplayName(UUID npcId, String displayName) {
        return delegate.get().updateDisplayName(npcId, displayName);
    }

    @Override
    public boolean teleport(UUID npcId, Location location) {
        return delegate.get().teleport(npcId, location);
    }
}
