package dev.patric.commonlib.api.module;

import java.util.List;
import java.util.Optional;

/**
 * Runtime registry exposing current module statuses.
 */
public interface ModuleRegistry {

    /**
     * Finds status for a module id.
     *
     * @param moduleId module id.
     * @return optional status.
     */
    Optional<ModuleStatus> find(String moduleId);

    /**
     * Lists all module statuses.
     *
     * @return immutable status list.
     */
    List<ModuleStatus> all();

    /**
     * Checks whether module is currently enabled.
     *
     * @param moduleId module id.
     * @return true when enabled.
     */
    boolean isEnabled(String moduleId);
}
