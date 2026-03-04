package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.port.PacketPort;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Packet port wrapper that can swap backend delegate at runtime.
 */
public final class DelegatingPacketPort implements PacketPort {

    private final PacketPort fallback;
    private final AtomicReference<PacketPort> delegate;

    /**
     * Creates a delegating packet port.
     *
     * @param fallback fallback backend.
     */
    public DelegatingPacketPort(PacketPort fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.delegate = new AtomicReference<>(fallback);
    }

    /**
     * Binds concrete backend delegate.
     *
     * @param next backend delegate.
     */
    public void bind(PacketPort next) {
        delegate.set(Objects.requireNonNull(next, "next"));
    }

    /**
     * Restores fallback backend.
     */
    public void resetToFallback() {
        delegate.set(fallback);
    }

    @Override
    public PacketListenerHandle register(PacketListenerOptions options, Consumer<PacketEnvelope> listener) {
        return delegate.get().register(options, listener);
    }

    @Override
    public boolean supportsMutation() {
        return delegate.get().supportsMutation();
    }

    @Override
    public void unregisterAll() {
        delegate.get().unregisterAll();
    }
}
