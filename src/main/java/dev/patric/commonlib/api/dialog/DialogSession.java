package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot of a dialog session.
 *
 * @param sessionId session id.
 * @param playerId owning player id.
 * @param templateKey template key.
 * @param status lifecycle status.
 * @param openedAtEpochMilli open timestamp.
 * @param lastInteractionEpochMilli last interaction timestamp.
 * @param timeoutTicks configured timeout in ticks.
 */
public record DialogSession(
        UUID sessionId,
        UUID playerId,
        String templateKey,
        DialogSessionStatus status,
        long openedAtEpochMilli,
        long lastInteractionEpochMilli,
        long timeoutTicks
) {

    /**
     * Compact constructor validation.
     */
    public DialogSession {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        templateKey = Objects.requireNonNull(templateKey, "templateKey").trim();
        if (templateKey.isEmpty()) {
            throw new IllegalArgumentException("templateKey must not be blank");
        }
        status = Objects.requireNonNull(status, "status");
        if (openedAtEpochMilli < 0L || lastInteractionEpochMilli < 0L) {
            throw new IllegalArgumentException("timestamps must be >= 0");
        }
        if (timeoutTicks < 0L) {
            throw new IllegalArgumentException("timeoutTicks must be >= 0");
        }
    }
}
