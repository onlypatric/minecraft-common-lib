package dev.patric.commonlib.runtime.persistence;

import dev.patric.commonlib.api.persistence.PersistenceRecord;
import dev.patric.commonlib.api.persistence.PersistenceWriteResult;
import dev.patric.commonlib.api.persistence.SqlPersistencePort;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * No-op SQL persistence abstraction used until a real SQL backend is installed.
 */
public final class NoopSqlPersistencePort implements SqlPersistencePort {

    @Override
    public CompletionStage<Optional<PersistenceRecord>> load(String namespace, String key) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletionStage<PersistenceWriteResult> save(PersistenceRecord record) {
        return CompletableFuture.completedFuture(PersistenceWriteResult.APPLIED);
    }

    @Override
    public CompletionStage<Boolean> delete(String namespace, String key) {
        return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    @Override
    public CompletionStage<List<PersistenceRecord>> list(String namespace) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean available() {
        return false;
    }
}
