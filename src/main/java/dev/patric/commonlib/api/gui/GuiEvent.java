package dev.patric.commonlib.api.gui;

import java.util.UUID;

/**
 * Portable GUI event contract.
 */
public sealed interface GuiEvent permits GuiClickEvent, GuiCloseEvent, GuiTimeoutEvent, GuiDisconnectEvent {

    /**
     * Returns the session targeted by the event.
     *
     * @return session id.
     */
    UUID sessionId();

    /**
     * Returns the expected session revision.
     *
     * @return expected revision.
     */
    long expectedRevision();
}
