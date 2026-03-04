package dev.patric.commonlib.api.packet;

import dev.patric.commonlib.api.port.PacketPort;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketApiContractTest {

    @Test
    void packetApiTypesAreLoadableAndContainExpectedMethods() throws Exception {
        assertTrue(PacketDirection.values().length >= 2);
        assertTrue(PacketListenerPriority.values().length >= 6);

        Method register = PacketPort.class.getMethod(
                "register",
                PacketListenerOptions.class,
                java.util.function.Consumer.class
        );
        assertEquals(PacketListenerHandle.class, register.getReturnType());

        Method supportsMutation = PacketPort.class.getMethod("supportsMutation");
        assertEquals(boolean.class, supportsMutation.getReturnType());

        Method unregisterAll = PacketPort.class.getMethod("unregisterAll");
        assertEquals(void.class, unregisterAll.getReturnType());

        Method close = PacketListenerHandle.class.getMethod("close");
        assertTrue(close.isDefault());

        PacketListenerOptions options = new PacketListenerOptions(
                PacketDirection.INBOUND,
                List.of("PLAY_CHAT"),
                PacketListenerPriority.HIGH,
                true
        );

        assertEquals(PacketDirection.INBOUND, options.direction());
        assertEquals(1, options.packetTypes().size());
        assertTrue(options.allowMutation());
    }

    @Test
    void envelopeContractAllowsCancelAndKeyValueExchange() {
        PacketEnvelope envelope = new PacketEnvelope() {
            private boolean cancelled;
            private String reason;
            private final java.util.Map<String, Object> values = new java.util.HashMap<>();

            @Override
            public UUID playerId() {
                return new UUID(0L, 1L);
            }

            @Override
            public String packetType() {
                return "PLAY_CHAT";
            }

            @Override
            public PacketDirection direction() {
                return PacketDirection.INBOUND;
            }

            @Override
            public Object nativePacket() {
                return new Object();
            }

            @Override
            public boolean cancelled() {
                return cancelled;
            }

            @Override
            public void cancel(String reason) {
                this.cancelled = true;
                this.reason = reason;
            }

            @Override
            public java.util.Optional<String> cancelReason() {
                return java.util.Optional.ofNullable(reason);
            }

            @Override
            public java.util.Optional<Object> read(String key) {
                return java.util.Optional.ofNullable(values.get(key));
            }

            @Override
            public boolean write(String key, Object value) {
                if (key == null || key.isBlank()) {
                    return false;
                }
                values.put(key, value);
                return true;
            }
        };

        assertFalse(envelope.cancelled());
        assertTrue(envelope.write("message", "hello"));
        assertEquals("hello", envelope.read("message").orElseThrow());

        envelope.cancel("blocked");
        assertTrue(envelope.cancelled());
        assertEquals("blocked", envelope.cancelReason().orElseThrow());
    }
}
