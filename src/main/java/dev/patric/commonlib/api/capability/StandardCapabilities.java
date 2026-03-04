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
    /** Command capability key. */
    public static final CapabilityKey<String> COMMAND = CapabilityKey.of("command", String.class);
    /** Scoreboard capability key. */
    public static final CapabilityKey<String> SCOREBOARD = CapabilityKey.of("scoreboard", String.class);
    /** Bossbar capability key. */
    public static final CapabilityKey<String> BOSSBAR = CapabilityKey.of("bossbar", String.class);
    /** Match engine capability key. */
    public static final CapabilityKey<String> MATCH_ENGINE = CapabilityKey.of("match_engine", String.class);
    /** Arena reset capability key. */
    public static final CapabilityKey<String> ARENA_RESET = CapabilityKey.of("arena_reset", String.class);
    /** YAML persistence capability key. */
    public static final CapabilityKey<String> PERSISTENCE_YAML = CapabilityKey.of("persistence_yaml", String.class);
    /** SQL persistence capability key. */
    public static final CapabilityKey<String> PERSISTENCE_SQL = CapabilityKey.of("persistence_sql", String.class);
    /** Team service capability key. */
    public static final CapabilityKey<String> TEAMS = CapabilityKey.of("teams", String.class);
    /** Party service capability key. */
    public static final CapabilityKey<String> PARTIES = CapabilityKey.of("parties", String.class);

    private StandardCapabilities() {
        throw new UnsupportedOperationException("Utility class");
    }
}
