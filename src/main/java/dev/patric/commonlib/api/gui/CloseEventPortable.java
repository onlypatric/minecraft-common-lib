package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable close request event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 * @param reason close reason.
 */
public record CloseEventPortable(
        UUID sessionId,
        long expectedRevision,
        GuiCloseReason reason
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public CloseEventPortable {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        reason = Objects.requireNonNull(reason, "reason");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
