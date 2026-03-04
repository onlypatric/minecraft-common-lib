package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable submit event.
 *
 * @param sessionId target session id.
 * @param expectedRevision optimistic expected revision.
 * @param submission submission payload.
 */
public record DialogSubmitEvent(
        UUID sessionId,
        long expectedRevision,
        DialogSubmission submission
) implements DialogEvent {

    /**
     * Compact constructor validation.
     */
    public DialogSubmitEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        submission = Objects.requireNonNull(submission, "submission");
        if (!sessionId.equals(submission.sessionId())) {
            throw new IllegalArgumentException("submission.sessionId must match sessionId");
        }
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
