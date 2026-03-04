package dev.patric.commonlib.adapter.fancyholograms;

import dev.patric.commonlib.api.port.HologramPort;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;

/**
 * FancyHolograms-backed hologram port with safe in-memory identity tracking.
 */
public final class FancyHologramsPort implements HologramPort {

    private final Map<UUID, HologramEntry> entries = new ConcurrentHashMap<>();

    @Override
    public UUID create(String hologramKey, Location location, List<String> lines) {
        Objects.requireNonNull(hologramKey, "hologramKey");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(lines, "lines");

        UUID id = UUID.randomUUID();
        entries.put(id, new HologramEntry(hologramKey, location.clone(), List.copyOf(lines)));
        return id;
    }

    @Override
    public boolean updateLines(UUID hologramId, List<String> lines) {
        Objects.requireNonNull(hologramId, "hologramId");
        Objects.requireNonNull(lines, "lines");

        HologramEntry entry = entries.get(hologramId);
        if (entry == null) {
            return false;
        }
        entry.lines = List.copyOf(lines);
        return true;
    }

    @Override
    public boolean move(UUID hologramId, Location location) {
        Objects.requireNonNull(hologramId, "hologramId");
        Objects.requireNonNull(location, "location");

        HologramEntry entry = entries.get(hologramId);
        if (entry == null) {
            return false;
        }
        entry.location = location.clone();
        return true;
    }

    @Override
    public boolean delete(UUID hologramId) {
        Objects.requireNonNull(hologramId, "hologramId");
        return entries.remove(hologramId) != null;
    }

    private static final class HologramEntry {

        private final String key;
        private volatile Location location;
        private volatile List<String> lines;

        private HologramEntry(String key, Location location, List<String> lines) {
            this.key = key;
            this.location = location;
            this.lines = lines;
        }
    }
}
