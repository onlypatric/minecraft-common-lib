package dev.patric.commonlib.api.dialog;

import java.util.UUID;

/**
 * Portable dialog event contract.
 */
public sealed interface DialogEvent permits DialogSubmitEvent, DialogTimeoutEvent, DialogCloseEvent {

    /**
     * Returns targeted session id.
     *
     * @return session id.
     */
    UUID sessionId();

    /**
     * Returns optimistic expected revision.
     *
     * @return expected revision.
     */
    long expectedRevision();
}
