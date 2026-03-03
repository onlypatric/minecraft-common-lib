package dev.patric.commonlib;

import dev.patric.commonlib.api.CommonRuntime;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry utility class for minecraft-common-lib.
 */
public final class CommonLib {

    private CommonLib() {
        throw new UnsupportedOperationException("CommonLib is a utility class and cannot be instantiated.");
    }

    /**
     * Creates a runtime builder bound to a plugin.
     *
     * @param plugin plugin instance.
     * @return runtime builder.
     */
    public static CommonRuntime.Builder runtime(JavaPlugin plugin) {
        return CommonRuntime.builder(plugin);
    }

    /**
     * Returns the library version.
     *
     * @return semantic version string.
     */
    public static String version() {
        Package pkg = CommonLib.class.getPackage();
        String implementationVersion = pkg == null ? null : pkg.getImplementationVersion();
        return implementationVersion == null ? "0.1.4-SNAPSHOT" : implementationVersion;
    }
}
