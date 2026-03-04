package dev.patric.commonlib.api.persistence;

/**
 * Single schema migration step.
 */
public interface SchemaMigration {

    /**
     * Source schema version.
     *
     * @return source version.
     */
    int fromVersion();

    /**
     * Target schema version.
     *
     * @return target version.
     */
    int toVersion();

    /**
     * Applies migration step.
     *
     * @param context migration context.
     */
    void migrate(SchemaMigrationContext context);
}
