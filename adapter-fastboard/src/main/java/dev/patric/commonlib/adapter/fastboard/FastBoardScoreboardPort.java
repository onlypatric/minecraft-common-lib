package dev.patric.commonlib.adapter.fastboard;

import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.ScoreboardPort;
import fr.mrmicky.fastboard.FastBoard;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Scoreboard port implementation backed by FastBoard.
 */
public final class FastBoardScoreboardPort implements ScoreboardPort {

    private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();

    @Override
    public boolean open(ScoreboardSession session) {
        Objects.requireNonNull(session, "session");

        Player player = Bukkit.getPlayer(session.playerId());
        if (player == null || !player.isOnline()) {
            return false;
        }

        try {
            FastBoard board = new FastBoard(player);
            board.updateTitle(session.snapshot().title());
            board.updateLines(session.snapshot().lines());

            FastBoard previous = boards.put(session.sessionId(), board);
            if (previous != null) {
                previous.delete();
            }
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(snapshot, "snapshot");

        FastBoard board = boards.get(sessionId);
        if (board == null) {
            return false;
        }

        try {
            board.updateTitle(snapshot.title());
            board.updateLines(snapshot.lines());
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");

        FastBoard board = boards.remove(sessionId);
        if (board == null) {
            return true;
        }

        try {
            board.delete();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }
}
