package dev.patric.commonlib.lifecycle.gui;

import dev.patric.commonlib.api.gui.GuiEvent;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Internal Bukkit event used to route portable GUI events through policy hooks.
 */
public final class GuiPolicyRoutedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GuiEvent guiEvent;
    private boolean cancelled;

    /**
     * Creates a routed event wrapper.
     *
     * @param guiEvent portable gui event.
     */
    public GuiPolicyRoutedEvent(GuiEvent guiEvent) {
        this.guiEvent = Objects.requireNonNull(guiEvent, "guiEvent");
    }

    /**
     * Returns wrapped portable event.
     *
     * @return wrapped gui event.
     */
    public GuiEvent guiEvent() {
        return guiEvent;
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
