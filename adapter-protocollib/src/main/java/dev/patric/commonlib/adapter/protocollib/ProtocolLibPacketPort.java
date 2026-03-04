package dev.patric.commonlib.adapter.protocollib;

import dev.patric.commonlib.api.packet.PacketDirection;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.port.PacketPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * ProtocolLib-backed packet port with generic listener registry and envelope helpers.
 */
public final class ProtocolLibPacketPort implements PacketPort {

    private final AtomicLong sequence = new AtomicLong(0L);
    private final Map<Long, RegisteredListener> listeners = new ConcurrentHashMap<>();
    private final boolean mutationSupported;

    /**
     * Creates a packet port with mutation enabled.
     */
    public ProtocolLibPacketPort() {
        this(true);
    }

    /**
     * Creates a packet port.
     *
     * @param mutationSupported whether payload mutation is allowed.
     */
    public ProtocolLibPacketPort(boolean mutationSupported) {
        this.mutationSupported = mutationSupported;
    }

    @Override
    public PacketListenerHandle register(PacketListenerOptions options, Consumer<PacketEnvelope> listener) {
        PacketPort.requireValidArgs(options, listener);

        long id = sequence.incrementAndGet();
        listeners.put(id, new RegisteredListener(options, listener));

        return () -> listeners.remove(id);
    }

    @Override
    public boolean supportsMutation() {
        return mutationSupported;
    }

    @Override
    public void unregisterAll() {
        listeners.clear();
    }

    /**
     * Dispatches envelope to compatible listeners.
     *
     * @param envelope packet envelope.
     */
    public void dispatch(PacketEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope");

        List<RegisteredListener> snapshot = new ArrayList<>(listeners.values());
        for (RegisteredListener registered : snapshot) {
            if (!directionMatches(registered.options.direction(), envelope.direction())) {
                continue;
            }
            if (!typeMatches(registered.options.packetTypes(), envelope.packetType())) {
                continue;
            }
            registered.listener.accept(envelope);
        }
    }

    /**
     * Current registered listener count.
     *
     * @return count.
     */
    public int listenerCount() {
        return listeners.size();
    }

    /**
     * Creates a simple mutable packet envelope.
     *
     * @param playerId player id.
     * @param packetType packet type.
     * @param direction packet direction.
     * @param nativePacket native packet object.
     * @return envelope instance.
     */
    public static PacketEnvelope envelope(
            UUID playerId,
            String packetType,
            PacketDirection direction,
            Object nativePacket
    ) {
        return new DefaultPacketEnvelope(playerId, packetType, direction, nativePacket);
    }

    private static boolean directionMatches(PacketDirection expected, PacketDirection actual) {
        return expected == actual;
    }

    private static boolean typeMatches(List<String> filters, String packetType) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        if (packetType == null || packetType.isBlank()) {
            return false;
        }
        for (String filter : filters) {
            if (packetType.equalsIgnoreCase(filter)) {
                return true;
            }
        }
        return false;
    }

    private record RegisteredListener(PacketListenerOptions options, Consumer<PacketEnvelope> listener) {
    }

    private static final class DefaultPacketEnvelope implements PacketEnvelope {

        private final UUID playerId;
        private final String packetType;
        private final PacketDirection direction;
        private final Object nativePacket;
        private final Map<String, Object> values = new ConcurrentHashMap<>();
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicReference<String> cancelReason = new AtomicReference<>(null);

        private DefaultPacketEnvelope(UUID playerId, String packetType, PacketDirection direction, Object nativePacket) {
            this.playerId = Objects.requireNonNull(playerId, "playerId");
            this.packetType = Objects.requireNonNull(packetType, "packetType");
            this.direction = Objects.requireNonNull(direction, "direction");
            this.nativePacket = nativePacket;
        }

        @Override
        public UUID playerId() {
            return playerId;
        }

        @Override
        public String packetType() {
            return packetType;
        }

        @Override
        public PacketDirection direction() {
            return direction;
        }

        @Override
        public Object nativePacket() {
            return nativePacket;
        }

        @Override
        public boolean cancelled() {
            return cancelled.get();
        }

        @Override
        public void cancel(String reason) {
            cancelled.set(true);
            cancelReason.set(reason == null || reason.isBlank() ? "cancelled" : reason.trim());
        }

        @Override
        public Optional<String> cancelReason() {
            return Optional.ofNullable(cancelReason.get());
        }

        @Override
        public Optional<Object> read(String key) {
            if (key == null || key.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public boolean write(String key, Object value) {
            if (key == null || key.isBlank()) {
                return false;
            }
            values.put(key, value);
            return true;
        }
    }
}
