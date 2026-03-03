package dev.patric.commonlib.testsupport;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonRuntime;
import java.util.Arrays;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Helpers for plugin-side tests using common runtime.
 */
public final class TestSupport {

    private TestSupport() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Builds runtime with provided components.
     *
     * @param plugin plugin instance.
     * @param components runtime components.
     * @return built runtime.
     */
    public static CommonRuntime runtime(JavaPlugin plugin, CommonComponent... components) {
        return CommonRuntime.builder(plugin)
                .components(Arrays.asList(components))
                .build();
    }
}
