package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable close event.
 *
 * @param sessionId target session id.
 * @param expectedRevision optimistic expected revision.
 * @param reason close reason.
 */
public record DialogCloseEvent(
        UUID sessionId,
        long expectedRevision,
        DialogCloseReason reason
) implements DialogEvent {

    /**
     * Compact constructor validation.
     */
    public DialogCloseEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        reason = Objects.requireNonNull(reason, "reason");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
