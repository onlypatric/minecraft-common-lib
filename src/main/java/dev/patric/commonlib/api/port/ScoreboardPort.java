package dev.patric.commonlib.api.port;

import java.util.List;
import java.util.UUID;

/**
 * Future scoreboard integration port.
 */
public interface ScoreboardPort {

    /**
     * Updates sidebar lines for a player.
     *
     * @param playerId player id.
     * @param title title.
     * @param lines lines.
     */
    void updateSidebar(UUID playerId, String title, List<String> lines);
}
