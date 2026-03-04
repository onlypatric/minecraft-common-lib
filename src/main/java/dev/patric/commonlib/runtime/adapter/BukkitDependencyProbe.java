package dev.patric.commonlib.runtime.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility used by adapter components to detect optional external plugin availability.
 */
public final class BukkitDependencyProbe {

    private static final Pattern VERSION_PART_PATTERN = Pattern.compile("(\\d+)");

    private BukkitDependencyProbe() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Probes plugin availability/version and optional API class presence.
     *
     * @param plugin owning plugin.
     * @param dependencyName dependency plugin name.
     * @param minimumVersion minimum supported version.
     * @param requiredClass optional required class.
     * @return probe result.
     */
    public static ProbeResult probe(
            JavaPlugin plugin,
            String dependencyName,
            String minimumVersion,
            String requiredClass
    ) {
        Objects.requireNonNull(plugin, "plugin");

        String name = requireText(dependencyName, "dependencyName");
        String min = requireText(minimumVersion, "minimumVersion");

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin dependency = pluginManager.getPlugin(name);
        if (dependency == null) {
            return ProbeResult.unavailable("missing-plugin:" + name);
        }

        if (!dependency.isEnabled()) {
            return ProbeResult.unavailable("disabled-plugin:" + name);
        }

        String installedVersion = dependency.getDescription().getVersion();
        if (installedVersion == null || installedVersion.isBlank()) {
            installedVersion = "unknown";
        }

        if (!isVersionAtLeast(installedVersion, min)) {
            return ProbeResult.unavailable("incompatible-version:" + name + ":" + installedVersion + "<" + min);
        }

        if (requiredClass != null && !requiredClass.isBlank()) {
            if (!isClassPresent(requiredClass.trim(), dependency.getClass().getClassLoader())) {
                return ProbeResult.unavailable("missing-class:" + requiredClass.trim());
            }
        }

        return ProbeResult.available(installedVersion);
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static boolean isClassPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static boolean isVersionAtLeast(String installed, String required) {
        List<Integer> installedParts = parseVersion(installed);
        List<Integer> requiredParts = parseVersion(required);

        if (installedParts.isEmpty() || requiredParts.isEmpty()) {
            return false;
        }

        int max = Math.max(installedParts.size(), requiredParts.size());
        for (int i = 0; i < max; i++) {
            int left = i < installedParts.size() ? installedParts.get(i) : 0;
            int right = i < requiredParts.size() ? requiredParts.get(i) : 0;
            if (left > right) {
                return true;
            }
            if (left < right) {
                return false;
            }
        }
        return true;
    }

    private static List<Integer> parseVersion(String value) {
        List<Integer> parts = new ArrayList<>();
        Matcher matcher = VERSION_PART_PATTERN.matcher(value == null ? "" : value);
        while (matcher.find()) {
            try {
                parts.add(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                return List.of();
            }
        }
        return parts;
    }

    /**
     * Probe outcome.
     *
     * @param available availability flag.
     * @param reason unavailable reason.
     * @param installedVersion detected plugin version.
     */
    public record ProbeResult(boolean available, String reason, String installedVersion) {

        /**
         * Creates available probe result.
         *
         * @param installedVersion installed plugin version.
         * @return result.
         */
        public static ProbeResult available(String installedVersion) {
            return new ProbeResult(true, null, installedVersion == null ? "unknown" : installedVersion);
        }

        /**
         * Creates unavailable probe result.
         *
         * @param reason unavailable reason.
         * @return result.
         */
        public static ProbeResult unavailable(String reason) {
            return new ProbeResult(false, Objects.requireNonNull(reason, "reason"), null);
        }
    }
}
