package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.CapabilityKey;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default runtime binder that swaps delegating ports and updates capability statuses.
 */
public final class DefaultPortBindingService implements PortBindingService {

    private final DelegatingCommandPort commandPort;
    private final DelegatingScoreboardPort scoreboardPort;
    private final DelegatingHologramPort hologramPort;
    private final DelegatingNpcPort npcPort;
    private final DelegatingClaimsPort claimsPort;
    private final DelegatingSchematicPort schematicPort;
    private final DelegatingBossBarPort bossBarPort;
    private final DelegatingMetricsPort metricsPort;
    private final DelegatingPacketPort packetPort;
    private final CapabilityRegistry capabilityRegistry;
    private final AtomicInteger schematicPriority;

    /**
     * Creates a default binding service.
     *
     * @param commandPort delegating command port.
     * @param scoreboardPort delegating scoreboard port.
     * @param hologramPort delegating hologram port.
     * @param npcPort delegating npc port.
     * @param claimsPort delegating claims port.
     * @param schematicPort delegating schematic port.
     * @param bossBarPort delegating bossbar port.
     * @param metricsPort delegating metrics port.
     * @param packetPort delegating packet port.
     * @param capabilityRegistry runtime capability registry.
     */
    public DefaultPortBindingService(
            DelegatingCommandPort commandPort,
            DelegatingScoreboardPort scoreboardPort,
            DelegatingHologramPort hologramPort,
            DelegatingNpcPort npcPort,
            DelegatingClaimsPort claimsPort,
            DelegatingSchematicPort schematicPort,
            DelegatingBossBarPort bossBarPort,
            DelegatingMetricsPort metricsPort,
            DelegatingPacketPort packetPort,
            CapabilityRegistry capabilityRegistry
    ) {
        this.commandPort = Objects.requireNonNull(commandPort, "commandPort");
        this.scoreboardPort = Objects.requireNonNull(scoreboardPort, "scoreboardPort");
        this.hologramPort = Objects.requireNonNull(hologramPort, "hologramPort");
        this.npcPort = Objects.requireNonNull(npcPort, "npcPort");
        this.claimsPort = Objects.requireNonNull(claimsPort, "claimsPort");
        this.schematicPort = Objects.requireNonNull(schematicPort, "schematicPort");
        this.bossBarPort = Objects.requireNonNull(bossBarPort, "bossBarPort");
        this.metricsPort = Objects.requireNonNull(metricsPort, "metricsPort");
        this.packetPort = Objects.requireNonNull(packetPort, "packetPort");
        this.capabilityRegistry = Objects.requireNonNull(capabilityRegistry, "capabilityRegistry");
        this.schematicPriority = new AtomicInteger(0);
    }

    @Override
    public void bindCommandPort(CommandPort port, String backendId, String backendVersion) {
        commandPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.COMMAND,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindScoreboardPort(ScoreboardPort port, String backendId, String backendVersion) {
        scoreboardPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.SCOREBOARD,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindHologramPort(HologramPort port, String backendId, String backendVersion) {
        hologramPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.HOLOGRAM,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindNpcPort(NpcPort port, String backendId, String backendVersion) {
        npcPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.NPC,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindClaimsPort(ClaimsPort port, String backendId, String backendVersion) {
        claimsPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.CLAIMS,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindSchematicPort(SchematicPort port, String backendId, String backendVersion) {
        Objects.requireNonNull(port, "port");
        int incomingPriority = schematicBindingPriority(backendId);

        synchronized (schematicPriority) {
            if (incomingPriority >= schematicPriority.get()) {
                schematicPort.bind(port);
                schematicPriority.set(incomingPriority);
                capabilityRegistry.publish(
                        StandardCapabilities.SCHEMATIC,
                        CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
                );
            }
        }
    }

    @Override
    public void bindBossBarPort(BossBarPort port, String backendId, String backendVersion) {
        bossBarPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.BOSSBAR,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindMetricsPort(MetricsPort port, String backendId, String backendVersion) {
        metricsPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.METRICS,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void bindPacketPort(PacketPort port, String backendId, String backendVersion) {
        packetPort.bind(Objects.requireNonNull(port, "port"));
        capabilityRegistry.publish(
                StandardCapabilities.PACKETS,
                CapabilityStatus.available(capabilityMetadata(backendId, backendVersion))
        );
    }

    @Override
    public void markUnavailable(CapabilityKey<String> key, String reason) {
        Objects.requireNonNull(key, "key");
        String normalizedReason = normalizeReason(reason);

        if (key.equals(StandardCapabilities.COMMAND)) {
            commandPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.SCOREBOARD)) {
            scoreboardPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.HOLOGRAM)) {
            hologramPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.NPC)) {
            npcPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.CLAIMS)) {
            claimsPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.SCHEMATIC)) {
            schematicPort.resetToFallback();
            schematicPriority.set(0);
        } else if (key.equals(StandardCapabilities.BOSSBAR)) {
            bossBarPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.METRICS)) {
            metricsPort.resetToFallback();
        } else if (key.equals(StandardCapabilities.PACKETS)) {
            packetPort.resetToFallback();
        }

        capabilityRegistry.publish(key, CapabilityStatus.unavailable(normalizedReason));
    }

    private static String capabilityMetadata(String backendId, String backendVersion) {
        String id = Objects.requireNonNull(backendId, "backendId").trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("backendId must not be blank");
        }

        String version = backendVersion == null ? "unknown" : backendVersion.trim();
        if (version.isEmpty()) {
            version = "unknown";
        }

        return id + ":" + version;
    }

    private static int schematicBindingPriority(String backendId) {
        String normalized = backendId == null ? "" : backendId.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("fawe")) {
            return 200;
        }
        if (normalized.equals("worldedit")) {
            return 100;
        }
        return 0;
    }

    private static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "unavailable:unknown";
        }
        return reason.trim();
    }
}
