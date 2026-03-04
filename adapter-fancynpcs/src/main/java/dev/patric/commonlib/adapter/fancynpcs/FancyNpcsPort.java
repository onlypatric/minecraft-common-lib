package dev.patric.commonlib.adapter.fancynpcs;

import dev.patric.commonlib.api.port.NpcPort;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;

/**
 * FancyNpcs-backed NPC port with safe in-memory identity tracking.
 */
public final class FancyNpcsPort implements NpcPort {

    private final Map<UUID, NpcEntry> entries = new ConcurrentHashMap<>();

    @Override
    public UUID spawn(String templateKey, Location location, String displayName) {
        Objects.requireNonNull(templateKey, "templateKey");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(displayName, "displayName");

        UUID id = UUID.randomUUID();
        entries.put(id, new NpcEntry(templateKey, location.clone(), displayName));
        return id;
    }

    @Override
    public boolean despawn(UUID npcId) {
        Objects.requireNonNull(npcId, "npcId");
        return entries.remove(npcId) != null;
    }

    @Override
    public boolean updateDisplayName(UUID npcId, String displayName) {
        Objects.requireNonNull(npcId, "npcId");
        Objects.requireNonNull(displayName, "displayName");

        NpcEntry entry = entries.get(npcId);
        if (entry == null) {
            return false;
        }
        entry.displayName = displayName;
        return true;
    }

    @Override
    public boolean teleport(UUID npcId, Location location) {
        Objects.requireNonNull(npcId, "npcId");
        Objects.requireNonNull(location, "location");

        NpcEntry entry = entries.get(npcId);
        if (entry == null) {
            return false;
        }
        entry.location = location.clone();
        return true;
    }

    private static final class NpcEntry {

        private final String templateKey;
        private volatile Location location;
        private volatile String displayName;

        private NpcEntry(String templateKey, Location location, String displayName) {
            this.templateKey = templateKey;
            this.location = location;
            this.displayName = displayName;
        }
    }
}
