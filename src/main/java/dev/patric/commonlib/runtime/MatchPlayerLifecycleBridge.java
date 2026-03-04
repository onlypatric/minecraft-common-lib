package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.match.MatchEngineService;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Built-in bridge that forwards player lifecycle events to match engine policy hooks.
 */
final class MatchPlayerLifecycleBridge implements Listener {

    private final MatchEngineService matchEngine;

    MatchPlayerLifecycleBridge(MatchEngineService matchEngine) {
        this.matchEngine = Objects.requireNonNull(matchEngine, "matchEngine");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        matchEngine.onPlayerQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        matchEngine.onPlayerWorldChange(event.getPlayer().getUniqueId());
    }
}
