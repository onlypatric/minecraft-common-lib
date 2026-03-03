package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.port.HologramPort;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

/**
 * No-op hologram port.
 */
public final class NoopHologramPort implements HologramPort {

    @Override
    public UUID create(String hologramKey, Location location, List<String> lines) {
        return UUID.randomUUID();
    }

    @Override
    public boolean updateLines(UUID hologramId, List<String> lines) {
        return false;
    }

    @Override
    public boolean move(UUID hologramId, Location location) {
        return false;
    }

    @Override
    public boolean delete(UUID hologramId) {
        return false;
    }
}
