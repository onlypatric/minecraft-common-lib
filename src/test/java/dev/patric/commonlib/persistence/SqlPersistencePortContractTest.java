package dev.patric.commonlib.persistence;

import dev.patric.commonlib.api.persistence.PersistenceRecord;
import dev.patric.commonlib.api.persistence.PersistenceWriteResult;
import dev.patric.commonlib.runtime.persistence.NoopSqlPersistencePort;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SqlPersistencePortContractTest {

    @Test
    void noopSqlPortRemainsSafeWhenNoAdapterIsConfigured() {
        NoopSqlPersistencePort port = new NoopSqlPersistencePort();

        assertFalse(port.available());
        assertEquals(java.util.Optional.empty(), port.load("ns", "k").toCompletableFuture().join());
        assertEquals(PersistenceWriteResult.APPLIED,
                port.save(new PersistenceRecord("ns", "k", Map.of("x", "1"), 1L)).toCompletableFuture().join());
        assertEquals(false, port.delete("ns", "k").toCompletableFuture().join());
        assertEquals(java.util.List.of(), port.list("ns").toCompletableFuture().join());
    }
}
