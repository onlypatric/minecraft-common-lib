package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Portable inventory drag event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 * @param slots involved raw slots.
 */
public record InventoryDragEventPortable(
        UUID sessionId,
        long expectedRevision,
        Set<Integer> slots
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public InventoryDragEventPortable {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        slots = Set.copyOf(slots == null ? Set.of() : slots);
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
