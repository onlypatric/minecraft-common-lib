package dev.patric.commonlib.external;

import dev.patric.commonlib.adapter.protocollib.ProtocolLibAdapterComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProtocolLibHandshakeTest {

    @Test
    void protocolLibVersionHandshakeSupportsStableAndDevStrategies() {
        assertTrue(ProtocolLibAdapterComponent.isVersionAtLeast("5.3.0", "5.3.0"));
        assertFalse(ProtocolLibAdapterComponent.isVersionAtLeast("5.2.9", "5.3.0"));

        String devRegex = "(?i).*(snapshot|dev|alpha|beta).*";
        assertTrue(ProtocolLibAdapterComponent.isDevVersionAllowed("5.3.0-SNAPSHOT", devRegex));
        assertTrue(ProtocolLibAdapterComponent.isDevVersionAllowed("5.4.0-dev-1", devRegex));
        assertFalse(ProtocolLibAdapterComponent.isDevVersionAllowed("5.3.0", devRegex));
    }
}
