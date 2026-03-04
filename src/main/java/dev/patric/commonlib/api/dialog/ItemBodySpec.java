package dev.patric.commonlib.api.dialog;

import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Item-rendered body block specification.
 *
 * @param item item stack to render.
 * @param description optional plain description.
 * @param showDecorations whether item decorations are visible.
 * @param showTooltip whether tooltip is shown.
 * @param width width in range [1,256].
 * @param height height in range [1,256].
 */
public record ItemBodySpec(
        ItemStack item,
        @Nullable PlainMessageBodySpec description,
        boolean showDecorations,
        boolean showTooltip,
        int width,
        int height
) implements DialogBodySpec {

    /**
     * Compact constructor validation.
     */
    public ItemBodySpec {
        item = Objects.requireNonNull(item, "item").clone();
        if (width < 1 || width > 256) {
            throw new IllegalArgumentException("width must be in range [1,256]");
        }
        if (height < 1 || height > 256) {
            throw new IllegalArgumentException("height must be in range [1,256]");
        }
    }
}
