package dev.patric.commonlib.lifecycle.dialog;

import dev.patric.commonlib.api.dialog.DialogEvent;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Internal Bukkit event used to route portable dialog events through policy hooks.
 */
public final class DialogPolicyRoutedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DialogEvent dialogEvent;
    private boolean cancelled;

    /**
     * Creates a routed event wrapper.
     *
     * @param dialogEvent portable dialog event.
     */
    public DialogPolicyRoutedEvent(DialogEvent dialogEvent) {
        this.dialogEvent = Objects.requireNonNull(dialogEvent, "dialogEvent");
    }

    /**
     * Returns wrapped portable event.
     *
     * @return wrapped dialog event.
     */
    public DialogEvent dialogEvent() {
        return dialogEvent;
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
