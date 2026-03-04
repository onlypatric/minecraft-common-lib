package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable disconnect event.
 *
 * @param sessionId session id.
 * @param expectedRevision expected revision.
 * @param playerId disconnecting player id.
 */
public record DisconnectEventPortable(
        UUID sessionId,
        long expectedRevision,
        UUID playerId
) implements GuiInteractionEvent {

    /**
     * Compact constructor validation.
     */
    public DisconnectEventPortable {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
