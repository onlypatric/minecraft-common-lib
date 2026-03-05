package dev.patric.commonlib.api.module;

/**
 * Runtime lifecycle state for a declared module.
 */
public enum ModuleState {
    /** Module declared and accepted by planner. */
    REGISTERED,
    /** Module skipped because one or more declared dependencies are missing. */
    SKIPPED_MISSING_DEPENDENCY,
    /** Module skipped because it is part of a dependency cycle. */
    SKIPPED_CYCLE,
    /** Module skipped because a dependency ended up inactive. */
    SKIPPED_DEPENDENCY_INACTIVE,
    /** Module failed during onLoad. */
    FAILED_LOAD,
    /** Module failed during onEnable. */
    FAILED_ENABLE,
    /** Module currently enabled. */
    ENABLED,
    /** Module was enabled and then disabled. */
    DISABLED
}
