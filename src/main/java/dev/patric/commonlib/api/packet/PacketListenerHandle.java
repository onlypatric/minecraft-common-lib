package dev.patric.commonlib.api.packet;

/**
 * Handle for an active packet listener.
 */
public interface PacketListenerHandle extends AutoCloseable {

    /**
     * Unregisters listener.
     */
    void unregister();

    @Override
    default void close() {
        unregister();
    }
}
