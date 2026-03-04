package dev.patric.commonlib.api.hud;

import java.util.Objects;

/**
 * Bossbar state payload.
 *
 * @param title display title.
 * @param progress progress in range [0.0, 1.0].
 * @param color bar color.
 * @param style bar style.
 * @param visible whether bar should be visible.
 */
public record BossBarState(String title, float progress, HudBarColor color, HudBarStyle style, boolean visible) {

    /**
     * Compact constructor validation.
     */
    public BossBarState {
        title = Objects.requireNonNull(title, "title");
        color = Objects.requireNonNull(color, "color");
        style = Objects.requireNonNull(style, "style");
        if (Float.isNaN(progress) || progress < 0.0f || progress > 1.0f) {
            throw new IllegalArgumentException("progress must be between 0.0 and 1.0");
        }
    }
}
