package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Packet backend integration port.
 */
public interface PacketPort {

    /**
     * Registers a packet listener.
     *
     * @param options registration options.
     * @param listener listener callback.
     * @return listener handle.
     */
    PacketListenerHandle register(PacketListenerOptions options, Consumer<PacketEnvelope> listener);

    /**
     * Indicates whether backend supports payload mutation.
     *
     * @return true when mutation is supported.
     */
    boolean supportsMutation();

    /**
     * Unregisters all active listeners.
     */
    void unregisterAll();

    /**
     * Guard helper for listener args.
     *
     * @param options registration options.
     * @param listener listener callback.
     */
    static void requireValidArgs(PacketListenerOptions options, Consumer<PacketEnvelope> listener) {
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(listener, "listener");
    }
}
