package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.port.PacketPort;
import java.util.function.Consumer;

/**
 * No-op packet port.
 */
public final class NoopPacketPort implements PacketPort {

    @Override
    public PacketListenerHandle register(PacketListenerOptions options, Consumer<PacketEnvelope> listener) {
        PacketPort.requireValidArgs(options, listener);
        return () -> {
            // no-op
        };
    }

    @Override
    public boolean supportsMutation() {
        return false;
    }

    @Override
    public void unregisterAll() {
        // no-op
    }
}
