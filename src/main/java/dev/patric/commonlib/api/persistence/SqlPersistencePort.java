package dev.patric.commonlib.api.persistence;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Asynchronous SQL persistence abstraction.
 */
public interface SqlPersistencePort {

    /**
     * Loads record from SQL backend.
     *
     * @param namespace namespace.
     * @param key record key.
     * @return completion stage with optional record.
     */
    CompletionStage<Optional<PersistenceRecord>> load(String namespace, String key);

    /**
     * Saves record on SQL backend.
     *
     * @param record record data.
     * @return completion stage with write result.
     */
    CompletionStage<PersistenceWriteResult> save(PersistenceRecord record);

    /**
     * Deletes record from SQL backend.
     *
     * @param namespace namespace.
     * @param key record key.
     * @return completion stage with delete outcome.
     */
    CompletionStage<Boolean> delete(String namespace, String key);

    /**
     * Lists records in namespace.
     *
     * @param namespace namespace.
     * @return completion stage with immutable record list.
     */
    CompletionStage<List<PersistenceRecord>> list(String namespace);

    /**
     * Indicates whether SQL backend is available.
     *
     * @return true when SQL implementation is available.
     */
    boolean available();
}
