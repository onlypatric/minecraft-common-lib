package dev.patric.commonlib.api.hud;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic bossbar service abstraction.
 */
public interface BossBarService {

    /**
     * Opens a bossbar session.
     *
     * @param request open request.
     * @return opened session snapshot.
     */
    BossBarSession open(BossBarOpenRequest request);

    /**
     * Finds bar by id.
     *
     * @param barId bar id.
     * @return optional bar snapshot.
     */
    Optional<BossBarSession> find(UUID barId);

    /**
     * Returns active bars for a player.
     *
     * @param playerId player id.
     * @return active bars.
     */
    List<BossBarSession> activeByPlayer(UUID playerId);

    /**
     * Applies bossbar update with policy throttling/deduping.
     *
     * @param barId bar id.
     * @param nextState next state.
     * @return update result.
     */
    BossBarUpdateResult update(UUID barId, BossBarState nextState);

    /**
     * Closes a specific bar.
     *
     * @param barId bar id.
     * @param reason close reason.
     * @return true if closed by this call.
     */
    boolean close(UUID barId, HudAudienceCloseReason reason);

    /**
     * Closes all bars for a player.
     *
     * @param playerId player id.
     * @param reason close reason.
     * @return closed count.
     */
    int closeAllByPlayer(UUID playerId, HudAudienceCloseReason reason);

    /**
     * Closes all bars.
     *
     * @param reason close reason.
     * @return closed count.
     */
    int closeAll(HudAudienceCloseReason reason);

    /**
     * Audience quit cleanup hook.
     *
     * @param playerId player id.
     */
    void onPlayerQuit(UUID playerId);

    /**
     * Audience world-change cleanup hook.
     *
     * @param playerId player id.
     */
    void onPlayerWorldChange(UUID playerId);

    /**
     * Returns active update policy.
     *
     * @return policy.
     */
    HudUpdatePolicy policy();
}
