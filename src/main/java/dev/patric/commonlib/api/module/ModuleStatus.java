package dev.patric.commonlib.api.module;

import java.util.Objects;
import java.util.Set;

/**
 * Immutable status snapshot for a module.
 *
 * @param id module id.
 * @param state current state.
 * @param reason optional reason string.
 * @param dependsOn declared dependencies.
 * @param updatedAtEpochMilli update timestamp in epoch millis.
 */
public record ModuleStatus(
        String id,
        ModuleState state,
        String reason,
        Set<String> dependsOn,
        long updatedAtEpochMilli
) {

    /**
     * Normalizes null values and defensively copies dependencies.
     */
    public ModuleStatus {
        id = Objects.requireNonNull(id, "id");
        state = Objects.requireNonNull(state, "state");
        reason = reason == null ? "" : reason;
        dependsOn = Set.copyOf(dependsOn == null ? Set.of() : dependsOn);
    }

    /**
     * Returns a new status with updated state and reason.
     *
     * @param nextState next state.
     * @param nextReason next reason.
     * @param atEpochMilli update timestamp.
     * @return updated status.
     */
    public ModuleStatus withState(ModuleState nextState, String nextReason, long atEpochMilli) {
        return new ModuleStatus(id, nextState, nextReason, dependsOn, atEpochMilli);
    }
}
