package dev.patric.commonlib.api.packet;

import java.util.Optional;
import java.util.UUID;

/**
 * Mutable packet envelope abstraction.
 */
public interface PacketEnvelope {

    /**
     * Player id for the packet audience/source.
     *
     * @return player id.
     */
    UUID playerId();

    /**
     * Packet type id.
     *
     * @return packet type.
     */
    String packetType();

    /**
     * Packet direction.
     *
     * @return direction.
     */
    PacketDirection direction();

    /**
     * Native packet object.
     *
     * @return native packet.
     */
    Object nativePacket();

    /**
     * Indicates whether the packet is cancelled.
     *
     * @return true when cancelled.
     */
    boolean cancelled();

    /**
     * Cancels packet processing.
     *
     * @param reason cancellation reason.
     */
    void cancel(String reason);

    /**
     * Cancellation reason if any.
     *
     * @return reason.
     */
    Optional<String> cancelReason();

    /**
     * Reads a value from mutable packet context.
     *
     * @param key field key.
     * @return optional value.
     */
    Optional<Object> read(String key);

    /**
     * Writes a value into mutable packet context.
     *
     * @param key field key.
     * @param value value to write.
     * @return true when write was accepted.
     */
    boolean write(String key, Object value);
}
