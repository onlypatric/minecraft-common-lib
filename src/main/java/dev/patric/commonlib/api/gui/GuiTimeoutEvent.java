package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable timeout event.
 *
 * @param sessionId target session id.
 * @param expectedRevision expected current revision.
 */
public record GuiTimeoutEvent(UUID sessionId, long expectedRevision) implements GuiEvent {

    /**
     * Compact constructor validation.
     */
    public GuiTimeoutEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
