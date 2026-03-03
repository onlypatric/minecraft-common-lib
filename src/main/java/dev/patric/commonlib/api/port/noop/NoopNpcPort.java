package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.port.NpcPort;
import java.util.UUID;
import org.bukkit.Location;

/**
 * No-op NPC port.
 */
public final class NoopNpcPort implements NpcPort {

    @Override
    public UUID spawn(String templateKey, Location location, String displayName) {
        return UUID.randomUUID();
    }

    @Override
    public boolean despawn(UUID npcId) {
        return false;
    }

    @Override
    public boolean updateDisplayName(UUID npcId, String displayName) {
        return false;
    }

    @Override
    public boolean teleport(UUID npcId, Location location) {
        return false;
    }
}
