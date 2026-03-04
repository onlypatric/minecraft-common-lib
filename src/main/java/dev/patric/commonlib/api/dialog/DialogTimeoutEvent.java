package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable timeout event.
 *
 * @param sessionId target session id.
 * @param expectedRevision optimistic expected revision.
 */
public record DialogTimeoutEvent(UUID sessionId, long expectedRevision) implements DialogEvent {

    /**
     * Compact constructor validation.
     */
    public DialogTimeoutEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
