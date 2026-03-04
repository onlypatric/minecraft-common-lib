package dev.patric.commonlib.api.adapter;

import dev.patric.commonlib.api.capability.CapabilityKey;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;

/**
 * Runtime service used by optional adapter components to bind concrete port backends.
 */
public interface PortBindingService {

    /**
     * Binds command backend implementation.
     *
     * @param port command backend.
     * @param backendId backend identifier.
     * @param backendVersion backend version.
     */
    void bindCommandPort(CommandPort port, String backendId, String backendVersion);

    /**
     * Binds scoreboard backend implementation.
     *
     * @param port scoreboard backend.
     * @param backendId backend identifier.
     * @param backendVersion backend version.
     */
    void bindScoreboardPort(ScoreboardPort port, String backendId, String backendVersion);

    /**
     * Binds hologram backend implementation.
     *
     * @param port hologram backend.
     * @param backendId backend identifier.
     * @param backendVersion backend version.
     */
    void bindHologramPort(HologramPort port, String backendId, String backendVersion);

    /**
     * Binds NPC backend implementation.
     *
     * @param port npc backend.
     * @param backendId backend identifier.
     * @param backendVersion backend version.
     */
    void bindNpcPort(NpcPort port, String backendId, String backendVersion);

    /**
     * Marks a capability as unavailable with a reason.
     *
     * @param key capability key.
     * @param reason unavailable reason.
     */
    void markUnavailable(CapabilityKey<String> key, String reason);
}
