package dev.patric.commonlib.api.gui;

import java.util.Map;

/**
 * GUI state update action.
 *
 * @param data key/value updates.
 * @param replace whether to replace current state or merge.
 */
public record UpdateStateAction(Map<String, String> data, boolean replace) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public UpdateStateAction {
        data = Map.copyOf(data == null ? Map.of() : data);
    }
}
