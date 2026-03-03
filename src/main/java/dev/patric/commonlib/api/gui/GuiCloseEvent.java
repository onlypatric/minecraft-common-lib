package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable close request event.
 *
 * @param sessionId target session id.
 * @param expectedRevision expected current revision.
 * @param reason close reason.
 */
public record GuiCloseEvent(UUID sessionId, long expectedRevision, GuiCloseReason reason) implements GuiEvent {

    /**
     * Compact constructor validation.
     */
    public GuiCloseEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        reason = Objects.requireNonNull(reason, "reason");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
