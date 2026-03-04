package dev.patric.commonlib.runtime;

import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Built-in bridge forwarding player quit events to dialog service.
 */
final class DialogPlayerLifecycleBridge implements Listener {

    private final DefaultDialogService dialogService;

    DialogPlayerLifecycleBridge(DefaultDialogService dialogService) {
        this.dialogService = Objects.requireNonNull(dialogService, "dialogService");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dialogService.onPlayerQuit(event.getPlayer().getUniqueId());
    }
}
