package dev.patric.commonlib.persistence;

import dev.patric.commonlib.api.persistence.PersistenceRecord;
import dev.patric.commonlib.api.persistence.PersistenceWriteResult;
import dev.patric.commonlib.runtime.persistence.DefaultYamlPersistencePort;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlPersistencePortTest {

    private ServerMock server;
    private TestPlugin plugin;

    @BeforeEach
    void setUp() throws IOException {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        deleteDirectory(plugin.getDataFolder().toPath());
    }

    @AfterEach
    void tearDown() throws IOException {
        Path path = plugin.getDataFolder().toPath();
        MockBukkit.unmock();
        deleteDirectory(path);
    }

    @Test
    void saveLoadListDeleteAndNamespaceIsolationWork() {
        DefaultYamlPersistencePort port = new DefaultYamlPersistencePort(plugin);

        PersistenceRecord first = new PersistenceRecord(
                "players",
                "alice",
                Map.of("coins", "10", "rank", "warrior"),
                100L
        );
        PersistenceRecord second = new PersistenceRecord(
                "matches",
                "m1",
                Map.of("state", "running"),
                101L
        );

        assertEquals(PersistenceWriteResult.APPLIED, port.save(first));
        assertEquals(PersistenceWriteResult.APPLIED, port.save(second));

        var loadedFirst = port.load("players", "alice").orElseThrow();
        assertEquals("10", loadedFirst.fields().get("coins"));
        assertEquals(1, port.list("players").size());
        assertEquals(1, port.list("matches").size());
        assertEquals(List.of(), port.list("unknown"));

        assertTrue(port.delete("players", "alice"));
        assertFalse(port.delete("players", "alice"));
        assertFalse(port.load("players", "alice").isPresent());
    }

    @Test
    void persistedDataCanBeReadByNewPortInstance() {
        DefaultYamlPersistencePort first = new DefaultYamlPersistencePort(plugin);
        PersistenceRecord record = new PersistenceRecord(
                "profiles",
                "p1",
                Map.of("name", "Patric", "wins", "42"),
                200L
        );

        assertEquals(PersistenceWriteResult.APPLIED, first.save(record));

        DefaultYamlPersistencePort second = new DefaultYamlPersistencePort(plugin);
        var loaded = second.load("profiles", "p1").orElseThrow();
        assertEquals("Patric", loaded.fields().get("name"));
        assertEquals("42", loaded.fields().get("wins"));
    }

    private static void deleteDirectory(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            stream.sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException ioEx) {
                throw ioEx;
            }
            throw ex;
        }
    }
}
