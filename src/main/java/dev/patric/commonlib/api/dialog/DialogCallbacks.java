package dev.patric.commonlib.api.dialog;

/**
 * Lifecycle callbacks for dialog sessions.
 */
public interface DialogCallbacks {

    /**
     * Invoked after successful open.
     *
     * @param session opened session snapshot.
     */
    default void onOpen(DialogSession session) {
        // no-op
    }

    /**
     * Invoked on successful submit.
     *
     * @param session session snapshot.
     * @param submission submission payload.
     */
    default void onSubmit(DialogSession session, DialogSubmission submission) {
        // no-op
    }

    /**
     * Invoked when session is closed.
     *
     * @param session session snapshot.
     * @param reason close reason.
     */
    default void onClose(DialogSession session, DialogCloseReason reason) {
        // no-op
    }

    /**
     * Invoked when callback pipeline raises an error.
     *
     * @param session session snapshot.
     * @param error error cause.
     */
    default void onError(DialogSession session, Throwable error) {
        // no-op
    }

    /**
     * Returns no-op callbacks instance.
     *
     * @return no-op callbacks.
     */
    static DialogCallbacks noop() {
        return new DialogCallbacks() {
            // no-op
        };
    }
}
