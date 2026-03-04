package dev.patric.commonlib.lifecycle.gui;

import dev.patric.commonlib.api.gui.GuiInteractionEvent;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Internal Bukkit event used to route v2 GUI interactions through policy hooks.
 */
public final class GuiInteractionPolicyRoutedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GuiInteractionEvent interactionEvent;
    private boolean cancelled;

    /**
     * Creates a routed interaction wrapper.
     *
     * @param interactionEvent portable interaction event.
     */
    public GuiInteractionPolicyRoutedEvent(GuiInteractionEvent interactionEvent) {
        this.interactionEvent = Objects.requireNonNull(interactionEvent, "interactionEvent");
    }

    /**
     * Returns wrapped interaction event.
     *
     * @return wrapped event.
     */
    public GuiInteractionEvent interactionEvent() {
        return interactionEvent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Bukkit static handlers accessor.
     *
     * @return handler list.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
