package dev.patric.commonlib.api.port;

import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

/**
 * Hologram integration port.
 */
public interface HologramPort {

    /**
     * Creates a hologram.
     *
     * @param hologramKey logical hologram key.
     * @param location spawn location.
     * @param lines initial lines.
     * @return hologram id.
     */
    UUID create(String hologramKey, Location location, List<String> lines);

    /**
     * Updates hologram lines.
     *
     * @param hologramId hologram id.
     * @param lines lines.
     * @return true on success.
     */
    boolean updateLines(UUID hologramId, List<String> lines);

    /**
     * Moves a hologram.
     *
     * @param hologramId hologram id.
     * @param location destination.
     * @return true on success.
     */
    boolean move(UUID hologramId, Location location);

    /**
     * Deletes a hologram.
     *
     * @param hologramId hologram id.
     * @return true when deleted.
     */
    boolean delete(UUID hologramId);
}
