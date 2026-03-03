package dev.patric.commonlib.api;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Centralized YAML configuration loading and reloading.
 */
public interface ConfigService {

    /**
     * Returns the main configuration.
     *
     * @return main configuration.
     */
    FileConfiguration main();

    /**
     * Loads a relative YAML file from plugin data folder.
     *
     * @param relativePath path relative to plugin data folder.
     * @return loaded file configuration.
     */
    FileConfiguration load(String relativePath);

    /**
     * Reloads all known configuration files.
     */
    void reloadAll();
}
