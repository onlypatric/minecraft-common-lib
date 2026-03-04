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
    /** GUI capability key. */
    public static final CapabilityKey<String> GUI = CapabilityKey.of("gui", String.class);
    /** Scoreboard capability key. */
    public static final CapabilityKey<String> SCOREBOARD = CapabilityKey.of("scoreboard", String.class);
    /** Bossbar capability key. */
    public static final CapabilityKey<String> BOSSBAR = CapabilityKey.of("bossbar", String.class);
    /** Match engine capability key. */
    public static final CapabilityKey<String> MATCH_ENGINE = CapabilityKey.of("match_engine", String.class);

    private StandardCapabilities() {
        throw new UnsupportedOperationException("Utility class");
    }
}
