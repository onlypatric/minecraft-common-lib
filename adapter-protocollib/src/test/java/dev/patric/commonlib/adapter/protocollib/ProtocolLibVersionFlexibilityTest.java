package dev.patric.commonlib.adapter.protocollib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolLibVersionFlexibilityTest {

    @Test
    void stableVersionComparisonAcceptsExpectedSemver() {
        assertTrue(ProtocolLibAdapterComponent.isVersionAtLeast("5.3.0", "5.3.0"));
        assertTrue(ProtocolLibAdapterComponent.isVersionAtLeast("5.3.1", "5.3.0"));
        assertFalse(ProtocolLibAdapterComponent.isVersionAtLeast("5.2.9", "5.3.0"));
    }

    @Test
    void devVersionRegexCanEnableNonStableBuilds() {
        String regex = "(?i).*(snapshot|dev|alpha|beta).*";

        assertTrue(ProtocolLibAdapterComponent.isDevVersionAllowed("5.3.0-SNAPSHOT", regex));
        assertTrue(ProtocolLibAdapterComponent.isDevVersionAllowed("5.4.0-dev-12", regex));
        assertFalse(ProtocolLibAdapterComponent.isDevVersionAllowed("5.3.0", regex));
    }
}
