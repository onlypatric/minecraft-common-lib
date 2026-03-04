package dev.patric.commonlib.runtime.persistence;

import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.persistence.SchemaMigration;
import dev.patric.commonlib.api.persistence.SchemaMigrationContext;
import dev.patric.commonlib.api.persistence.SchemaMigrationService;
import dev.patric.commonlib.api.persistence.SqlPersistencePort;
import dev.patric.commonlib.api.persistence.YamlPersistencePort;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Default schema migration service using integer versions per namespace.
 */
public final class DefaultSchemaMigrationService implements SchemaMigrationService {

    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_.-]+");

    private final JavaPlugin plugin;
    private final YamlPersistencePort yaml;
    private final SqlPersistencePort sql;
    private final ServiceRegistry services;
    private final Map<String, List<SchemaMigration>> migrationsByNamespace;

    /**
     * Creates schema migration service.
     *
     * @param plugin owning plugin.
     * @param yaml yaml persistence.
     * @param sql sql persistence.
     * @param services runtime services.
     */
    public DefaultSchemaMigrationService(
            JavaPlugin plugin,
            YamlPersistencePort yaml,
            SqlPersistencePort sql,
            ServiceRegistry services
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.yaml = Objects.requireNonNull(yaml, "yaml");
        this.sql = Objects.requireNonNull(sql, "sql");
        this.services = Objects.requireNonNull(services, "services");
        this.migrationsByNamespace = new ConcurrentHashMap<>();
    }

    @Override
    public int currentVersion(String namespace) {
        String ns = sanitizeNamespace(namespace);
        YamlConfiguration config = loadVersionConfig();
        return config.getInt("versions." + ns, 0);
    }

    @Override
    public void register(String namespace, SchemaMigration migration) {
        String ns = sanitizeNamespace(namespace);
        Objects.requireNonNull(migration, "migration");

        if (migration.fromVersion() < 0 || migration.toVersion() < 0) {
            throw new IllegalArgumentException("Migration versions must be >= 0");
        }
        if (migration.toVersion() != migration.fromVersion() + 1) {
            throw new IllegalArgumentException("Migration must increment exactly by one version");
        }

        migrationsByNamespace.compute(ns, (ignored, existing) -> {
            List<SchemaMigration> next = existing == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(existing);
            boolean duplicateFrom = next.stream().anyMatch(step -> step.fromVersion() == migration.fromVersion());
            if (duplicateFrom) {
                throw new IllegalStateException(
                        "Duplicate migration from version " + migration.fromVersion() + " for namespace " + ns
                );
            }
            next.add(migration);
            next.sort(Comparator.comparingInt(SchemaMigration::fromVersion));
            return List.copyOf(next);
        });
    }

    @Override
    public int migrateToLatest(String namespace) {
        String ns = sanitizeNamespace(namespace);
        List<SchemaMigration> migrations = migrationsByNamespace.getOrDefault(ns, List.of());
        if (migrations.isEmpty()) {
            return currentVersion(ns);
        }

        Map<Integer, SchemaMigration> byFrom = new HashMap<>();
        int maxTarget = 0;
        for (SchemaMigration migration : migrations) {
            SchemaMigration previous = byFrom.putIfAbsent(migration.fromVersion(), migration);
            if (previous != null) {
                throw new IllegalStateException("Duplicate migration chain for namespace " + ns);
            }
            maxTarget = Math.max(maxTarget, migration.toVersion());
        }

        int current = currentVersion(ns);
        while (true) {
            SchemaMigration step = byFrom.get(current);
            if (step == null) {
                break;
            }
            SchemaMigrationContext context = new SchemaMigrationContext(ns, yaml, sql, services);
            step.migrate(context);
            current = step.toVersion();
            saveCurrentVersion(ns, current);
        }

        if (current < maxTarget) {
            throw new IllegalStateException(
                    "Missing migration step for namespace " + ns + " at version " + current
            );
        }

        return current;
    }

    private YamlConfiguration loadVersionConfig() {
        File file = versionFile();
        ensureFileExists(file);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveCurrentVersion(String namespace, int version) {
        File file = versionFile();
        ensureFileExists(file);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("versions." + namespace, version);
        try {
            config.save(file);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot save schema versions", ex);
        }
    }

    private File versionFile() {
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Cannot create plugin data folder: " + folder);
        }
        return new File(folder, "schema-versions.yml");
    }

    private static void ensureFileExists(File file) {
        if (file.exists()) {
            return;
        }
        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Cannot create schema version file: " + file);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create schema version file: " + file, ex);
        }
    }

    private static String sanitizeNamespace(String namespace) {
        String normalized = Objects.requireNonNull(namespace, "namespace").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        if (!SAFE_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("namespace contains unsupported characters: " + normalized);
        }
        return normalized;
    }
}
