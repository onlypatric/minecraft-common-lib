package dev.patric.commonlib.api.gui;

import java.util.Objects;
import java.util.UUID;

/**
 * Portable click event.
 *
 * @param sessionId target session id.
 * @param expectedRevision expected current revision.
 * @param action click action.
 * @param slot slot index.
 */
public record GuiClickEvent(UUID sessionId, long expectedRevision, ClickAction action, int slot) implements GuiEvent {

    /**
     * Compact constructor validation.
     */
    public GuiClickEvent {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        action = Objects.requireNonNull(action, "action");
        if (expectedRevision < 0L) {
            throw new IllegalArgumentException("expectedRevision must be >= 0");
        }
    }
}
