package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.gui.GuiInteractionEvent;
import dev.patric.commonlib.api.gui.GuiInteractionResult;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.GuiSessionStatus;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.SlotClickEvent;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuiPlayerInventoryBridgeInteractionTest {

    @Test
    void clickEventIsForwardedAndCancelledWhenDenied() {
        GuiSessionService guiService = mock(GuiSessionService.class);
        GuiPlayerInventoryBridge bridge = new GuiPlayerInventoryBridge(guiService);

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);

        UUID sessionId = UUID.randomUUID();
        GuiSession session = new GuiSession(
                sessionId,
                playerId,
                "menu.test",
                GuiState.empty(),
                GuiSessionStatus.OPEN,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L
        );
        when(guiService.activeByPlayer(playerId)).thenReturn(List.of(session));
        when(guiService.interact(any(SlotClickEvent.class))).thenReturn(GuiInteractionResult.DENIED_BY_POLICY);

        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getClick()).thenReturn(ClickType.LEFT);
        when(event.getAction()).thenReturn(InventoryAction.PICKUP_ALL);
        when(event.getRawSlot()).thenReturn(10);

        bridge.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(guiService).interact(any(SlotClickEvent.class));
    }

    @Test
    void dragEventIsForwardedAndCancelledWhenDenied() {
        GuiSessionService guiService = mock(GuiSessionService.class);
        GuiPlayerInventoryBridge bridge = new GuiPlayerInventoryBridge(guiService);

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);

        UUID sessionId = UUID.randomUUID();
        GuiSession session = new GuiSession(
                sessionId,
                playerId,
                "menu.test",
                GuiState.empty(),
                GuiSessionStatus.OPEN,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L
        );
        when(guiService.activeByPlayer(playerId)).thenReturn(List.of(session));
        when(guiService.interact(any())).thenReturn(GuiInteractionResult.DENIED_BY_POLICY);

        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getRawSlots()).thenReturn(Set.of(1, 2));

        bridge.onInventoryDrag(event);

        verify(event).setCancelled(true);
        verify(guiService).interact(any());
    }

    @Test
    void closeEventIsForwardedAsInteractionEvent() {
        GuiSessionService guiService = mock(GuiSessionService.class);
        GuiPlayerInventoryBridge bridge = new GuiPlayerInventoryBridge(guiService);

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);

        UUID sessionId = UUID.randomUUID();
        GuiSession session = new GuiSession(
                sessionId,
                playerId,
                "menu.test",
                GuiState.empty(),
                GuiSessionStatus.OPEN,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L
        );
        when(guiService.activeByPlayer(playerId)).thenReturn(List.of(session));
        when(guiService.interact(any())).thenReturn(GuiInteractionResult.APPLIED);

        InventoryCloseEvent event = mock(InventoryCloseEvent.class);
        when(event.getPlayer()).thenReturn(player);

        bridge.onInventoryClose(event);

        ArgumentCaptor<GuiInteractionEvent> captor = ArgumentCaptor.forClass(GuiInteractionEvent.class);
        verify(guiService).interact(captor.capture());
        assertTrue(captor.getValue().sessionId().equals(sessionId));
    }
}
