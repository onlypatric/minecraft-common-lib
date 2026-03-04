package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable timeout event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 */
public record TimeoutEventPortable(
        UUID sessionId,
        long expectedRevision
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public TimeoutEventPortable {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
