package dev.patric.commonlib.adapter.bossbar.paper;

import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.port.BossBarPort;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * Paper bossbar adapter implementation.
 */
public final class PaperBossBarPort implements BossBarPort {

    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();

    @Override
    public boolean open(BossBarSession session) {
        Objects.requireNonNull(session, "session");

        Player player = Bukkit.getPlayer(session.playerId());
        if (player == null || !player.isOnline()) {
            return false;
        }

        try {
            BossBar bar = Bukkit.createBossBar(
                    session.state().title(),
                    toBarColor(session.state().color()),
                    toBarStyle(session.state().style())
            );
            bar.setProgress(clampProgress(session.state().progress()));
            bar.setVisible(session.state().visible());
            bar.addPlayer(player);

            BossBar previous = bars.put(session.barId(), bar);
            if (previous != null) {
                previous.removeAll();
            }
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean render(UUID barId, BossBarState state) {
        Objects.requireNonNull(barId, "barId");
        Objects.requireNonNull(state, "state");

        BossBar bar = bars.get(barId);
        if (bar == null) {
            return false;
        }

        try {
            bar.setTitle(state.title());
            bar.setColor(toBarColor(state.color()));
            bar.setStyle(toBarStyle(state.style()));
            bar.setProgress(clampProgress(state.progress()));
            bar.setVisible(state.visible());
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean close(UUID barId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(barId, "barId");
        Objects.requireNonNull(reason, "reason");

        BossBar bar = bars.remove(barId);
        if (bar == null) {
            return true;
        }

        try {
            bar.removeAll();
            bar.setVisible(false);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private static double clampProgress(float progress) {
        return Math.max(0.0D, Math.min(1.0D, progress));
    }

    private static BarColor toBarColor(HudBarColor color) {
        return switch (color) {
            case PINK -> BarColor.PINK;
            case BLUE -> BarColor.BLUE;
            case RED -> BarColor.RED;
            case GREEN -> BarColor.GREEN;
            case YELLOW -> BarColor.YELLOW;
            case PURPLE -> BarColor.PURPLE;
            case WHITE -> BarColor.WHITE;
        };
    }

    private static BarStyle toBarStyle(HudBarStyle style) {
        return switch (style) {
            case SOLID -> BarStyle.SOLID;
            case SEGMENTED_6 -> BarStyle.SEGMENTED_6;
            case SEGMENTED_10 -> BarStyle.SEGMENTED_10;
            case SEGMENTED_12 -> BarStyle.SEGMENTED_12;
            case SEGMENTED_20 -> BarStyle.SEGMENTED_20;
        };
    }
}
