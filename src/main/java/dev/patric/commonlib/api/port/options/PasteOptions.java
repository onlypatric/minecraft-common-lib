package dev.patric.commonlib.api.port.options;

/**
 * Paste configuration options for schematic operations.
 *
 * @param ignoreAir whether air blocks should be ignored.
 * @param copyBiomes whether biome data should be copied.
 * @param replaceEntities whether entities in target area should be replaced.
 * @param maxBlocks max block limit allowed for an operation.
 */
public record PasteOptions(boolean ignoreAir, boolean copyBiomes, boolean replaceEntities, int maxBlocks) {

    /**
     * Default conservative options.
     *
     * @return default options instance.
     */
    public static PasteOptions defaults() {
        return new PasteOptions(false, true, false, 5_000_000);
    }
}
