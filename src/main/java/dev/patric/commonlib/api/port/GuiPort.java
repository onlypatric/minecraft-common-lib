package dev.patric.commonlib.api.port;

import java.util.UUID;

/**
 * Future GUI integration port.
 */
public interface GuiPort {

    /**
     * Opens a menu for a player.
     *
     * @param playerId player id.
     * @param viewKey view key.
     */
    void open(UUID playerId, String viewKey);
}
