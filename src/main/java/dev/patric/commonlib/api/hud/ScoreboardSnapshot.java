package dev.patric.commonlib.api.hud;

import java.util.List;
import java.util.Objects;

/**
 * Scoreboard payload snapshot.
 *
 * @param title title.
 * @param lines lines.
 */
public record ScoreboardSnapshot(String title, List<String> lines) {

    /**
     * Compact constructor validation.
     */
    public ScoreboardSnapshot {
        title = Objects.requireNonNull(title, "title");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    }
}
