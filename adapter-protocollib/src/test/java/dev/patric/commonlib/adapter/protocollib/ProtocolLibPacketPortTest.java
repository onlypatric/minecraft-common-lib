package dev.patric.commonlib.adapter.protocollib;

import dev.patric.commonlib.api.packet.PacketDirection;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.packet.PacketListenerPriority;
import dev.patric.commonlib.api.port.PacketPort;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolLibPacketPortTest {

    @Test
    void listenerRegistrationDispatchAndUnregisterAllAreDeterministic() {
        ProtocolLibPacketPort port = new ProtocolLibPacketPort(true);
        AtomicInteger seen = new AtomicInteger();

        PacketListenerOptions options = new PacketListenerOptions(
                PacketDirection.INBOUND,
                List.of("PLAY_CHAT"),
                PacketListenerPriority.NORMAL,
                true
        );

        var handle = port.register(options, envelope -> {
            seen.incrementAndGet();
            envelope.write("content", "hello");
            envelope.cancel("blocked");
        });

        PacketEnvelope matching = ProtocolLibPacketPort.envelope(
                UUID.randomUUID(),
                "PLAY_CHAT",
                PacketDirection.INBOUND,
                new Object()
        );

        port.dispatch(matching);

        assertEquals(1, seen.get());
        assertTrue(matching.cancelled());
        assertEquals("blocked", matching.cancelReason().orElseThrow());
        assertEquals("hello", matching.read("content").orElseThrow());
        assertTrue(port.supportsMutation());

        handle.unregister();
        assertEquals(0, port.listenerCount());

        PacketPort.requireValidArgs(options, envelope -> {
            // no-op
        });

        port.unregisterAll();
        assertEquals(0, port.listenerCount());
    }

    @Test
    void packetTypeAndDirectionFiltersAreEnforced() {
        ProtocolLibPacketPort port = new ProtocolLibPacketPort(false);
        AtomicInteger seen = new AtomicInteger();

        PacketListenerOptions options = new PacketListenerOptions(
                PacketDirection.OUTBOUND,
                List.of("ENTITY_METADATA"),
                PacketListenerPriority.HIGH,
                false
        );

        port.register(options, envelope -> seen.incrementAndGet());

        port.dispatch(ProtocolLibPacketPort.envelope(
                UUID.randomUUID(),
                "PLAY_CHAT",
                PacketDirection.OUTBOUND,
                new Object()
        ));
        port.dispatch(ProtocolLibPacketPort.envelope(
                UUID.randomUUID(),
                "ENTITY_METADATA",
                PacketDirection.INBOUND,
                new Object()
        ));
        port.dispatch(ProtocolLibPacketPort.envelope(
                UUID.randomUUID(),
                "ENTITY_METADATA",
                PacketDirection.OUTBOUND,
                new Object()
        ));

        assertEquals(1, seen.get());
        assertFalse(port.supportsMutation());
    }
}
