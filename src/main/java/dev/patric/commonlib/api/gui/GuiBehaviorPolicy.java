package dev.patric.commonlib.api.gui;

/**
 * Global behavior flags applied to one GUI definition.
 *
 * @param lockUnknownSlots whether unknown slots are denied by default.
 * @param allowInventoryDrag whether drag operations are allowed by default.
 * @param allowDoubleClick whether double-click collection is allowed.
 */
public record GuiBehaviorPolicy(
        boolean lockUnknownSlots,
        boolean allowInventoryDrag,
        boolean allowDoubleClick
) {

    /**
     * Returns the default policy.
     *
     * @return default policy.
     */
    public static GuiBehaviorPolicy defaults() {
        return strictDefaults();
    }

    /**
     * Returns strict defaults for competitive GUI behavior.
     *
     * @return strict policy.
     */
    public static GuiBehaviorPolicy strictDefaults() {
        return new GuiBehaviorPolicy(true, false, false);
    }
}
