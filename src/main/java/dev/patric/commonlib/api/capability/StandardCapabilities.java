package dev.patric.commonlib.api.capability;

/**
 * Built-in capability keys for plugin-generic ports.
 */
public final class StandardCapabilities {

    /** NPC capability key. */
    public static final CapabilityKey<String> NPC = CapabilityKey.of("npc", String.class);
    /** Hologram capability key. */
    public static final CapabilityKey<String> HOLOGRAM = CapabilityKey.of("hologram", String.class);
    /** Claims capability key. */
    public static final CapabilityKey<String> CLAIMS = CapabilityKey.of("claims", String.class);
    /** Schematic capability key. */
    public static final CapabilityKey<String> SCHEMATIC = CapabilityKey.of("schematic", String.class);

    private StandardCapabilities() {
        throw new UnsupportedOperationException("Utility class");
    }
}
