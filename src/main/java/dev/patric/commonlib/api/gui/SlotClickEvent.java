package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable slot click event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 * @param slot slot index.
 * @param action click action.
 * @param transferType transfer intent.
 */
public record SlotClickEvent(
        UUID sessionId,
        long expectedRevision,
        int slot,
        ClickAction action,
        SlotTransferType transferType
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public SlotClickEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        action = Objects.requireNonNull(action, "action");
        transferType = transferType == null ? SlotTransferType.NONE : transferType;
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be >= 0");
        }
    }
}
