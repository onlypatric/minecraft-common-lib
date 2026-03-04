package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable double-click event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 * @param slot slot index.
 */
public record DoubleClickEventPortable(
        UUID sessionId,
        long expectedRevision,
        int slot
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public DoubleClickEventPortable {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be >= 0");
        }
    }
}
