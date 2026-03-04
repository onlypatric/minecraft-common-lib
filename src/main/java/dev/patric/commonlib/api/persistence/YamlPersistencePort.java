package dev.patric.commonlib.api.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Synchronous YAML persistence port.
 */
public interface YamlPersistencePort {

    /**
     * Loads a record by namespace and key.
     *
     * @param namespace namespace.
     * @param key record key.
     * @return optional record.
     */
    Optional<PersistenceRecord> load(String namespace, String key);

    /**
     * Saves a persistence record.
     *
     * @param record record data.
     * @return write result.
     */
    PersistenceWriteResult save(PersistenceRecord record);

    /**
     * Deletes record by namespace and key.
     *
     * @param namespace namespace.
     * @param key record key.
     * @return true when record existed.
     */
    boolean delete(String namespace, String key);

    /**
     * Lists records by namespace.
     *
     * @param namespace namespace.
     * @return immutable list of records.
     */
    List<PersistenceRecord> list(String namespace);
}
