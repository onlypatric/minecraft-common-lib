package dev.patric.commonlib.runtime.persistence;

import dev.patric.commonlib.api.persistence.PersistenceRecord;
import dev.patric.commonlib.api.persistence.PersistenceWriteResult;
import dev.patric.commonlib.api.persistence.YamlPersistencePort;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Default YAML persistence implementation backed by per-namespace files.
 */
public final class DefaultYamlPersistencePort implements YamlPersistencePort {

    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_.-]+");

    private final JavaPlugin plugin;

    /**
     * Creates a YAML persistence port.
     *
     * @param plugin owning plugin.
     */
    public DefaultYamlPersistencePort(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public synchronized Optional<PersistenceRecord> load(String namespace, String key) {
        try {
            String ns = sanitizeName(namespace, "namespace");
            String recordKey = sanitizeName(key, "key");
            YamlConfiguration config = loadNamespaceConfig(ns);
            ConfigurationSection section = config.getConfigurationSection("records." + recordKey);
            if (section == null) {
                return Optional.empty();
            }

            ConfigurationSection fieldsSection = section.getConfigurationSection("fields");
            Map<String, String> fields = new LinkedHashMap<>();
            if (fieldsSection != null) {
                for (String fieldKey : fieldsSection.getKeys(false)) {
                    Object value = fieldsSection.get(fieldKey);
                    fields.put(fieldKey, value == null ? "" : String.valueOf(value));
                }
            }

            long updatedAt = section.getLong("updatedAt", 0L);
            return Optional.of(new PersistenceRecord(ns, recordKey, fields, updatedAt));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public synchronized PersistenceWriteResult save(PersistenceRecord record) {
        Objects.requireNonNull(record, "record");

        final String namespace;
        final String recordKey;
        try {
            namespace = sanitizeName(record.namespace(), "namespace");
            recordKey = sanitizeName(record.key(), "key");
        } catch (IllegalArgumentException ex) {
            return PersistenceWriteResult.VALIDATION_FAILED;
        }

        try {
            YamlConfiguration config = loadNamespaceConfig(namespace);
            String root = "records." + recordKey;
            config.set(root + ".updatedAt", record.updatedAtEpochMilli());
            config.set(root + ".fields", null);
            for (Map.Entry<String, String> entry : record.fields().entrySet()) {
                String fieldKey;
                try {
                    fieldKey = sanitizeName(entry.getKey(), "field");
                } catch (IllegalArgumentException ex) {
                    return PersistenceWriteResult.VALIDATION_FAILED;
                }
                config.set(root + ".fields." + fieldKey, entry.getValue());
            }

            saveNamespaceConfig(namespace, config);
            return PersistenceWriteResult.APPLIED;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot save persistence record", ex);
        }
    }

    @Override
    public synchronized boolean delete(String namespace, String key) {
        final String ns;
        final String recordKey;
        try {
            ns = sanitizeName(namespace, "namespace");
            recordKey = sanitizeName(key, "key");
        } catch (IllegalArgumentException ex) {
            return false;
        }

        try {
            YamlConfiguration config = loadNamespaceConfig(ns);
            String root = "records." + recordKey;
            if (!config.contains(root)) {
                return false;
            }
            config.set(root, null);
            saveNamespaceConfig(ns, config);
            return true;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot delete persistence record", ex);
        }
    }

    @Override
    public synchronized List<PersistenceRecord> list(String namespace) {
        final String ns;
        try {
            ns = sanitizeName(namespace, "namespace");
        } catch (IllegalArgumentException ex) {
            return List.of();
        }

        YamlConfiguration config = loadNamespaceConfig(ns);
        ConfigurationSection records = config.getConfigurationSection("records");
        if (records == null) {
            return List.of();
        }

        List<PersistenceRecord> found = new ArrayList<>();
        Set<String> keys = records.getKeys(false);
        for (String key : keys) {
            load(ns, key).ifPresent(found::add);
        }

        return List.copyOf(found);
    }

    private YamlConfiguration loadNamespaceConfig(String namespace) {
        File file = namespaceFile(namespace);
        ensureFileExists(file);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveNamespaceConfig(String namespace, YamlConfiguration config) throws IOException {
        File file = namespaceFile(namespace);
        ensureFileExists(file);
        config.save(file);
    }

    private File namespaceFile(String namespace) {
        File root = new File(plugin.getDataFolder(), "persistence");
        if (!root.exists() && !root.mkdirs()) {
            throw new IllegalStateException("Cannot create persistence root: " + root);
        }
        return new File(root, namespace + ".yml");
    }

    private static void ensureFileExists(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create parent directory: " + parent);
        }
        if (file.exists()) {
            return;
        }
        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Cannot create file: " + file);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create file: " + file, ex);
        }
    }

    private static String sanitizeName(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (!SAFE_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException(fieldName + " contains unsupported characters: " + normalized);
        }
        return normalized;
    }
}
