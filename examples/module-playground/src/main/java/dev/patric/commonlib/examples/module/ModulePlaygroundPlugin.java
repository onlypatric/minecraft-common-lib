package dev.patric.commonlib.examples.module;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.bootstrap.RuntimeBootstrap;
import dev.patric.commonlib.api.error.OperationResult;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModuleRegistry;
import dev.patric.commonlib.api.module.ModuleStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example plugin demonstrating the common-lib module system.
 */
public final class ModulePlaygroundPlugin extends JavaPlugin {

    private CommonRuntime runtime;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        OperationResult<CommonRuntime> built = RuntimeBootstrap.build(this, builder -> {
            builder.includeDefaultCoreComponents(false);
            builder.enableModuleDiagnostics(true);
            builder.modules(buildModules());
        });

        if (built.isFailure()) {
            getLogger().severe("Unable to build runtime: " + built.errorOrNull().message());
            runtime = null;
            return;
        }

        runtime = built.valueOrNull();
        OperationResult<Void> loadResult = RuntimeBootstrap.safeLoad(runtime);
        if (loadResult.isFailure()) {
            getLogger().severe("Unable to load runtime: " + loadResult.errorOrNull().message());
            runtime = null;
        }
    }

    @Override
    public void onEnable() {
        if (runtime == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        OperationResult<Void> enableResult = RuntimeBootstrap.safeEnable(runtime);
        if (enableResult.isFailure()) {
            getLogger().severe("Unable to enable runtime: " + enableResult.errorOrNull().message());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        emitModuleDump("enable");
    }

    @Override
    public void onDisable() {
        if (runtime != null) {
            RuntimeBootstrap.safeDisable(runtime);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("clibmodule")) {
            return false;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("dump")) {
            sender.sendMessage("Usage: /clibmodule dump");
            return true;
        }

        emitModuleDump("manual");
        sender.sendMessage("clibmodule: module dump emitted");
        return true;
    }

    private List<CommonModule> buildModules() {
        boolean failingDemoEnabled = getConfig().getBoolean("failingDemo.enabled", false);

        return List.of(
                new CoreServicesModule(),
                new EconomyHooksModule(),
                new FailingDemoModule(failingDemoEnabled)
        );
    }

    private void emitModuleDump(String origin) {
        if (runtime == null) {
            return;
        }

        ModuleRegistry registry = runtime.services().require(ModuleRegistry.class);
        for (ModuleStatus status : registry.all()) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("origin", origin);
            payload.put("id", status.id());
            payload.put("state", status.state().name());
            payload.put("reason", status.reason());
            payload.put("dependsOn", status.dependsOn());
            getLogger().info("CLIB_PLAYGROUND_MODULES " + toJson(payload));
        }
    }

    private static String toJson(Map<String, Object> values) {
        StringBuilder out = new StringBuilder();
        out.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            out.append('"').append(escape(entry.getKey())).append('"').append(':');
            appendJsonValue(out, entry.getValue());
        }
        out.append('}');
        return out.toString();
    }

    private static void appendJsonValue(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
            return;
        }
        if (value instanceof Boolean || value instanceof Number) {
            out.append(value);
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            out.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                appendJsonValue(out, item);
            }
            out.append(']');
            return;
        }

        out.append('"').append(escape(String.valueOf(value))).append('"');
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class CoreServicesModule implements CommonModule {

        @Override
        public String id() {
            return "core-services";
        }

        @Override
        public String description() {
            return "Provides base runtime services";
        }
    }

    private static final class EconomyHooksModule implements CommonModule {

        @Override
        public String id() {
            return "economy-hooks";
        }

        @Override
        public Set<String> dependsOn() {
            return Set.of("core-services");
        }

        @Override
        public String description() {
            return "Example dependent module";
        }
    }

    private static final class FailingDemoModule implements CommonModule {

        private final boolean enabled;

        private FailingDemoModule(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String id() {
            return "failing-demo";
        }

        @Override
        public Set<String> dependsOn() {
            return Set.of("core-services");
        }

        @Override
        public void onEnable(CommonContext ctx) {
            if (enabled) {
                throw new IllegalStateException("failing-demo enabled from config");
            }
        }

        @Override
        public String description() {
            return "Optional module used to verify soft-disable";
        }
    }
}
