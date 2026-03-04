package dev.patric.commonlib.runtime.adapter;

import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.CapabilityKey;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import java.util.Locale;
import java.util.Objects;

/**
 * Default runtime binder that swaps delegating ports and updates capability statuses.
 */
public final class DefaultPortBindingService implements PortBindingService {

    private final DelegatingCommandPort commandPort;
    private final DelegatingScoreboardPort scoreboardPort;
    private final DelegatingHologramPort hologramPort;
    private final DelegatingNpcPort npcPort;
    private final CapabilityRegistry capabilityRegistry;

    /**
     * Creates a default binding service.
     *
     * @param commandPort delegating command port.
     * @param scoreboardPort delegating scoreboard port.
     * @param hologramPort delegating hologram port.
     * @param npcPort delegating npc port.
     * @param capabilityRegistry runtime capability registry.
     */
    public DefaultPortBindingService(
            DelegatingCommandPort commandPort,
            DelegatingScoreboardPort scoreboardPort,
            DelegatingHologramPort hologramPort,
            DelegatingNpcPort npcPort,
            CapabilityRegistry capabilityRegistry
    ) {
        this.commandPort = Objects.requireNonNull(commandPort, "commandPort");
        this.scoreboardPort = Objects.requireNonNull(scoreboardPort, "scoreboardPort");
        this.hologramPort = Objects.requireNonNull(hologramPort, "hologramPort");
        this.npcPort = Objects.requireNonNull(npcPort, "npcPort");
        this.capabilityRegistry = Objects.requireNonNull(capabilityRegistry, "capabilityRegistry");
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

    private static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "unavailable:unknown";
        }
        return reason.trim();
    }
}
