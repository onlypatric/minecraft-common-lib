package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.CloseEventPortable;
import dev.patric.commonlib.api.gui.DoubleClickEventPortable;
import dev.patric.commonlib.api.gui.DropEventPortable;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiInteractionResult;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.HotbarSwapEventPortable;
import dev.patric.commonlib.api.gui.InventoryDragEventPortable;
import dev.patric.commonlib.api.gui.SlotClickEvent;
import dev.patric.commonlib.api.gui.SlotTransferType;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Built-in bridge that forwards Bukkit inventory events to the GUI interaction pipeline.
 */
final class GuiPlayerInventoryBridge implements Listener {

    private final GuiSessionService guiService;

    GuiPlayerInventoryBridge(GuiSessionService guiService) {
        this.guiService = guiService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Optional<GuiSession> active = pickSession(player.getUniqueId());
        if (active.isEmpty()) {
            return;
        }

        GuiSession session = active.get();
        GuiInteractionResult result;

        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            result = guiService.interact(new DoubleClickEventPortable(
                    session.sessionId(),
                    session.state().revision(),
                    normalizeSlot(event.getRawSlot())
            ));
        } else if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
            result = guiService.interact(new DropEventPortable(
                    session.sessionId(),
                    session.state().revision(),
                    normalizeSlot(event.getRawSlot())
            ));
        } else if (event.getAction() == InventoryAction.HOTBAR_SWAP
                || event.getClick() == ClickType.NUMBER_KEY) {
            result = guiService.interact(new HotbarSwapEventPortable(
                    session.sessionId(),
                    session.state().revision(),
                    normalizeSlot(event.getRawSlot()),
                    Math.max(0, event.getHotbarButton())
            ));
        } else {
            result = guiService.interact(new SlotClickEvent(
                    session.sessionId(),
                    session.state().revision(),
                    normalizeSlot(event.getRawSlot()),
                    mapClick(event.getClick()),
                    mapTransfer(event.getAction())
            ));
        }

        if (result == GuiInteractionResult.DENIED_BY_POLICY || result == GuiInteractionResult.INVALID_ACTION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Optional<GuiSession> active = pickSession(player.getUniqueId());
        if (active.isEmpty()) {
            return;
        }
        GuiSession session = active.get();
        GuiInteractionResult result = guiService.interact(new InventoryDragEventPortable(
                session.sessionId(),
                session.state().revision(),
                event.getRawSlots()
        ));
        if (result == GuiInteractionResult.DENIED_BY_POLICY || result == GuiInteractionResult.INVALID_ACTION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Optional<GuiSession> active = pickSession(player.getUniqueId());
        active.ifPresent(session -> guiService.interact(new CloseEventPortable(
                session.sessionId(),
                session.state().revision(),
                GuiCloseReason.USER_CLOSE
        )));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        guiService.closeAllByPlayer(event.getPlayer().getUniqueId(), GuiCloseReason.DISCONNECT);
    }

    private Optional<GuiSession> pickSession(UUID playerId) {
        return guiService.activeByPlayer(playerId).stream()
                .max(Comparator.comparingLong(GuiSession::openedAtEpochMilli));
    }

    private static int normalizeSlot(int rawSlot) {
        return Math.max(0, rawSlot);
    }

    private static ClickAction mapClick(ClickType clickType) {
        return switch (clickType) {
            case LEFT -> ClickAction.LEFT;
            case RIGHT -> ClickAction.RIGHT;
            case SHIFT_LEFT -> ClickAction.SHIFT_LEFT;
            case SHIFT_RIGHT -> ClickAction.SHIFT_RIGHT;
            case MIDDLE -> ClickAction.MIDDLE;
            case DROP, CONTROL_DROP -> ClickAction.DROP;
            default -> ClickAction.UNKNOWN;
        };
    }

    private static SlotTransferType mapTransfer(InventoryAction action) {
        return switch (action) {
            case PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME, MOVE_TO_OTHER_INVENTORY, COLLECT_TO_CURSOR -> SlotTransferType.TAKE;
            case PLACE_ALL, PLACE_SOME, PLACE_ONE -> SlotTransferType.DEPOSIT;
            case SWAP_WITH_CURSOR, HOTBAR_SWAP -> SlotTransferType.TAKE_DEPOSIT;
            default -> SlotTransferType.NONE;
        };
    }
}
