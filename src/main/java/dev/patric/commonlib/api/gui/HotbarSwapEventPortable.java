package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable hotbar swap event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 * @param slot slot index.
 * @param hotbarButton target hotbar index.
 */
public record HotbarSwapEventPortable(
        UUID sessionId,
        long expectedRevision,
        int slot,
        int hotbarButton
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public HotbarSwapEventPortable {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be >= 0");
        }
        if (hotbarButton < 0 || hotbarButton > 8) {
            throw new IllegalArgumentException("hotbarButton must be between 0 and 8");
        }
    }
}
