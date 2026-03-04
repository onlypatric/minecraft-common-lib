package dev.patric.commonlib.api.packet;

import java.util.List;
import java.util.Objects;

/**
 * Packet listener registration options.
 *
 * @param direction traffic direction.
 * @param packetTypes backend packet type identifiers.
 * @param priority listener priority.
 * @param allowMutation whether payload mutation is allowed.
 */
public record PacketListenerOptions(
        PacketDirection direction,
        List<String> packetTypes,
        PacketListenerPriority priority,
        boolean allowMutation
) {

    /**
     * Compact constructor normalization.
     */
    public PacketListenerOptions {
        direction = Objects.requireNonNull(direction, "direction");
        priority = Objects.requireNonNull(priority, "priority");
        packetTypes = packetTypes == null ? List.of() : List.copyOf(packetTypes);
    }
}
