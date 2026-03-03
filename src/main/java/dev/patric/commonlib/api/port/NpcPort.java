package dev.patric.commonlib.api.port;

import java.util.UUID;
import org.bukkit.Location;

/**
 * NPC integration port.
 */
public interface NpcPort {

    /**
     * Spawns an NPC from a template.
     *
     * @param templateKey template identifier.
     * @param location spawn location.
     * @param displayName display name.
     * @return spawned npc id.
     */
    UUID spawn(String templateKey, Location location, String displayName);

    /**
     * Despawns an NPC.
     *
     * @param npcId npc id.
     * @return true when npc was removed.
     */
    boolean despawn(UUID npcId);

    /**
     * Updates display name for an NPC.
     *
     * @param npcId npc id.
     * @param displayName new display name.
     * @return true when update succeeded.
     */
    boolean updateDisplayName(UUID npcId, String displayName);

    /**
     * Teleports an NPC.
     *
     * @param npcId npc id.
     * @param location destination.
     * @return true when move succeeded.
     */
    boolean teleport(UUID npcId, Location location);
}
