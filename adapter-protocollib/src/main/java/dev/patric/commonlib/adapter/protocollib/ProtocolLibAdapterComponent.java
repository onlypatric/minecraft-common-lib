package dev.patric.commonlib.adapter.protocollib;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.PacketPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional adapter component that binds ProtocolLib-backed packet port when available.
 */
public final class ProtocolLibAdapterComponent implements CommonComponent {

    /** Adapter id. */
    public static final String ID = "adapter-protocollib";
    /** Minimum supported ProtocolLib version (stable path). */
    public static final String MINIMUM_VERSION = "5.3.0";

    private static final Pattern VERSION_PART_PATTERN = Pattern.compile("(\\d+)");

    private final Function<JavaPlugin, ProbeResult> probe;
    private final Supplier<PacketPort> portFactory;

    /**
     * Creates component with default probe and backend factory.
     */
    public ProtocolLibAdapterComponent() {
        this(
                plugin -> probeProtocolLib(
                        plugin,
                        MINIMUM_VERSION,
                        plugin.getConfig().getString(
                                "commonlib.adapters.protocollib.devVersionRegex",
                                "(?i).*(snapshot|alpha|beta|dev).*"
                        )
                ),
                ProtocolLibPacketPort::new
        );
    }

    /**
     * Creates component with injected probe/factory (useful for tests).
     *
     * @param probe probe function.
     * @param portFactory port factory.
     */
    public ProtocolLibAdapterComponent(
            Function<JavaPlugin, ProbeResult> probe,
            Supplier<PacketPort> portFactory
    ) {
        this.probe = Objects.requireNonNull(probe, "probe");
        this.portFactory = Objects.requireNonNull(portFactory, "portFactory");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEnable(CommonContext context) {
        PortBindingService bindingService = context.services().require(PortBindingService.class);

        ProbeResult probeResult;
        try {
            probeResult = probe.apply(context.plugin());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.PACKETS,
                    "binding-failed:protocollib:" + ex.getClass().getSimpleName()
            );
            return;
        }

        if (!probeResult.available()) {
            bindingService.markUnavailable(StandardCapabilities.PACKETS, probeResult.reason());
            return;
        }

        try {
            bindingService.bindPacketPort(portFactory.get(), "protocollib", probeResult.installedVersion());
        } catch (RuntimeException ex) {
            bindingService.markUnavailable(
                    StandardCapabilities.PACKETS,
                    "binding-failed:protocollib:" + ex.getClass().getSimpleName()
            );
        }
    }

    /**
     * Probe ProtocolLib plugin and classpath compatibility.
     *
     * @param plugin owner plugin.
     * @param minimumVersion minimum stable version.
     * @param devVersionRegex optional dev regex allowance.
     * @return probe result.
     */
    public static ProbeResult probeProtocolLib(JavaPlugin plugin, String minimumVersion, String devVersionRegex) {
        Objects.requireNonNull(plugin, "plugin");

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin dependency = pluginManager.getPlugin("ProtocolLib");
        if (dependency == null) {
            return ProbeResult.unavailable("missing-plugin:ProtocolLib");
        }

        if (!dependency.isEnabled()) {
            return ProbeResult.unavailable("disabled-plugin:ProtocolLib");
        }

        String installedVersion = dependency.getDescription().getVersion();
        if (installedVersion == null || installedVersion.isBlank()) {
            installedVersion = "unknown";
        }

        boolean versionCompatible = isVersionAtLeast(installedVersion, minimumVersion);
        boolean devAllowed = isDevVersionAllowed(installedVersion, devVersionRegex);
        if (!versionCompatible && !devAllowed) {
            return ProbeResult.unavailable(
                    "incompatible-version:ProtocolLib:" + installedVersion + "<" + minimumVersion
            );
        }

        if (!isClassPresent("com.comphenix.protocol.ProtocolLibrary", dependency.getClass().getClassLoader())) {
            return ProbeResult.unavailable("missing-class:com.comphenix.protocol.ProtocolLibrary");
        }

        return ProbeResult.available(installedVersion);
    }

    private static boolean isClassPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static boolean isVersionAtLeast(String installed, String required) {
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

    public static boolean isDevVersionAllowed(String version, String devVersionRegex) {
        if (version == null || version.isBlank() || devVersionRegex == null || devVersionRegex.isBlank()) {
            return false;
        }

        Pattern pattern = Pattern.compile(devVersionRegex);
        return pattern.matcher(version).matches();
    }

    private static List<Integer> parseVersion(String value) {
        List<Integer> parts = new ArrayList<>();
        Matcher matcher = VERSION_PART_PATTERN.matcher(value == null ? "" : value);
        while (matcher.find()) {
            try {
                parts.add(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ex) {
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
     * @param installedVersion detected installed version.
     */
    public record ProbeResult(boolean available, String reason, String installedVersion) {

        /**
         * Creates available probe result.
         *
         * @param installedVersion detected installed version.
         * @return probe result.
         */
        public static ProbeResult available(String installedVersion) {
            return new ProbeResult(true, null, installedVersion == null ? "unknown" : installedVersion);
        }

        /**
         * Creates unavailable probe result.
         *
         * @param reason unavailable reason.
         * @return probe result.
         */
        public static ProbeResult unavailable(String reason) {
            return new ProbeResult(false, Objects.requireNonNull(reason, "reason"), null);
        }
    }
}
