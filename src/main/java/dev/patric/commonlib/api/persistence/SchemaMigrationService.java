package dev.patric.commonlib.api.persistence;

/**
 * Integer schema version migration service.
 */
public interface SchemaMigrationService {

    /**
     * Returns current schema version for namespace.
     *
     * @param namespace namespace.
     * @return current schema version.
     */
    int currentVersion(String namespace);

    /**
     * Registers migration step for namespace.
     *
     * @param namespace namespace.
     * @param migration migration step.
     */
    void register(String namespace, SchemaMigration migration);

    /**
     * Applies migrations to latest version for namespace.
     *
     * @param namespace namespace.
     * @return resulting schema version.
     */
    int migrateToLatest(String namespace);
}
