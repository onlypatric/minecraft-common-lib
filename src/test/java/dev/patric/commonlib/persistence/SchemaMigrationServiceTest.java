package dev.patric.commonlib.persistence;

import dev.patric.commonlib.api.persistence.PersistenceRecord;
import dev.patric.commonlib.api.persistence.SchemaMigration;
import dev.patric.commonlib.runtime.persistence.DefaultSchemaMigrationService;
import dev.patric.commonlib.runtime.persistence.DefaultYamlPersistencePort;
import dev.patric.commonlib.runtime.persistence.NoopSqlPersistencePort;
import dev.patric.commonlib.services.DefaultServiceRegistry;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchemaMigrationServiceTest {

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
    void migrationChainRunsInOrderAndUpdatesCurrentVersion() {
        DefaultYamlPersistencePort yaml = new DefaultYamlPersistencePort(plugin);
        DefaultSchemaMigrationService service = new DefaultSchemaMigrationService(
                plugin,
                yaml,
                new NoopSqlPersistencePort(),
                new DefaultServiceRegistry()
        );

        service.register("game", migration(0, 1, context -> context.yaml().save(new PersistenceRecord(
                context.namespace(),
                "state",
                Map.of("phase", "one"),
                1L
        ))));
        service.register("game", migration(1, 2, context -> {
            var previous = context.yaml().load(context.namespace(), "state").orElseThrow();
            context.yaml().save(new PersistenceRecord(
                    context.namespace(),
                    "state",
                    Map.of("phase", "two", "prev", previous.fields().getOrDefault("phase", "missing")),
                    2L
            ));
        }));

        int finalVersion = service.migrateToLatest("game");
        assertEquals(2, finalVersion);
        assertEquals(2, service.currentVersion("game"));

        var state = yaml.load("game", "state").orElseThrow();
        assertEquals("two", state.fields().get("phase"));
        assertEquals("one", state.fields().get("prev"));

        assertEquals(2, service.migrateToLatest("game"));
    }

    @Test
    void missingMigrationStepFailsFast() {
        DefaultSchemaMigrationService service = new DefaultSchemaMigrationService(
                plugin,
                new DefaultYamlPersistencePort(plugin),
                new NoopSqlPersistencePort(),
                new DefaultServiceRegistry()
        );

        service.register("broken", migration(1, 2, context -> {
        }));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.migrateToLatest("broken"));
        assertEquals(true, ex.getMessage().contains("Missing migration step"));
    }

    private static SchemaMigration migration(int from, int to, java.util.function.Consumer<dev.patric.commonlib.api.persistence.SchemaMigrationContext> body) {
        return new SchemaMigration() {
            @Override
            public int fromVersion() {
                return from;
            }

            @Override
            public int toVersion() {
                return to;
            }

            @Override
            public void migrate(dev.patric.commonlib.api.persistence.SchemaMigrationContext context) {
                body.accept(context);
            }
        };
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
