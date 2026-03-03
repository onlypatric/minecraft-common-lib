package dev.patric.commonlib.config;

import dev.patric.commonlib.api.ConfigService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * YAML-backed configuration service with lazy loading and reload support.
 */
public final class YamlConfigService implements ConfigService {

    private final JavaPlugin plugin;
    private final String mainPath;
    private final Set<String> knownFiles;
    private final Map<String, FileConfiguration> cache;

    /**
     * Creates a YAML configuration service.
     *
     * @param plugin owning plugin.
     * @param mainPath relative path to main config file.
     * @param additionalPaths additional config paths to preload on reload.
     */
    public YamlConfigService(JavaPlugin plugin, String mainPath, Iterable<String> additionalPaths) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.mainPath = sanitizePath(mainPath);
        this.knownFiles = new LinkedHashSet<>();
        this.cache = new ConcurrentHashMap<>();
        this.knownFiles.add(this.mainPath);
        for (String additionalPath : additionalPaths) {
            this.knownFiles.add(sanitizePath(additionalPath));
        }
    }

    @Override
    public FileConfiguration main() {
        return load(mainPath);
    }

    @Override
    public FileConfiguration load(String relativePath) {
        String normalized = sanitizePath(relativePath);
        knownFiles.add(normalized);
        return cache.computeIfAbsent(normalized, this::loadFresh);
    }

    @Override
    public void reloadAll() {
        Set<String> snapshot = Set.copyOf(knownFiles);
        for (String file : snapshot) {
            cache.put(file, loadFresh(file));
        }
    }

    private FileConfiguration loadFresh(String relativePath) {
        ensureFileExists(relativePath);
        File file = new File(plugin.getDataFolder(), relativePath);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void ensureFileExists(String relativePath) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Cannot create plugin data folder: " + dataFolder);
        }

        File target = new File(dataFolder, relativePath);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create config parent directory: " + parent);
        }

        if (target.exists()) {
            return;
        }

        try (InputStream in = plugin.getResource(relativePath)) {
            if (in != null) {
                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else if (!target.createNewFile()) {
                throw new IllegalStateException("Cannot create config file: " + target);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot initialize config file: " + target, ex);
        }
    }

    private static String sanitizePath(String relativePath) {
        String value = Objects.requireNonNull(relativePath, "relativePath").replace('\\', '/').trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Config path cannot be empty");
        }
        if (value.startsWith("/") || value.contains("..")) {
            throw new IllegalArgumentException("Config path must be relative and safe: " + value);
        }
        return value;
    }
}
