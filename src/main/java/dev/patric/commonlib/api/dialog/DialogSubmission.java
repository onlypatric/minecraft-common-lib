package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import java.util.UUID;

/**
 * Payload emitted when a dialog action is submitted.
 *
 * @param sessionId session id.
 * @param playerId player id.
 * @param actionId action identifier.
 * @param response typed response wrapper.
 * @param occurredAtEpochMilli submission timestamp.
 */
public record DialogSubmission(
        UUID sessionId,
        UUID playerId,
        String actionId,
        DialogResponse response,
        long occurredAtEpochMilli
) {

    /**
     * Compact constructor validation.
     */
    public DialogSubmission {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        actionId = Objects.requireNonNull(actionId, "actionId").trim();
        if (actionId.isEmpty()) {
            throw new IllegalArgumentException("actionId must not be blank");
        }
        response = Objects.requireNonNull(response, "response");
        if (occurredAtEpochMilli < 0L) {
            throw new IllegalArgumentException("occurredAtEpochMilli must be >= 0");
        }
    }
}
