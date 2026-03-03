package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable disconnect event.
 *
 * @param sessionId target session id.
 * @param expectedRevision expected current revision.
 * @param playerId disconnecting player id.
 */
public record GuiDisconnectEvent(UUID sessionId, long expectedRevision, UUID playerId) implements GuiEvent {

    /**
     * Compact constructor validation.
     */
    public GuiDisconnectEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
