package dev.patric.commonlib.api.gui;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable GUI state payload.
 *
 * @param revision monotonic revision used for optimistic concurrency checks.
 * @param data serializable key/value state.
 */
public record GuiState(long revision, Map<String, String> data) {

    /**
     * Compact constructor validation.
     */
    public GuiState {
        if (revision < 0L) {
            throw new IllegalArgumentException("revision must be >= 0");
        }
        data = Map.copyOf(Objects.requireNonNull(data, "data"));
    }

    /**
     * Creates an empty state with revision 0.
     *
     * @return empty state.
     */
    public static GuiState empty() {
        return new GuiState(0L, Map.of());
    }

    /**
     * Creates a state with caller-provided revision and data.
     *
     * @param revision revision number.
     * @param data state payload.
     * @return state instance.
     */
    public static GuiState withData(long revision, Map<String, String> data) {
        return new GuiState(revision, data);
    }
}
