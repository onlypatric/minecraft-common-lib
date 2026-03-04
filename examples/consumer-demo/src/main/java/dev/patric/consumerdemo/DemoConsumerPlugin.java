package dev.patric.consumerdemo;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.bootstrap.RuntimeBootstrap;
import dev.patric.commonlib.api.error.OperationResult;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Minimal consumer plugin that integrates minecraft-common-lib via RuntimeBootstrap.
 */
public class DemoConsumerPlugin extends JavaPlugin {

    private CommonRuntime runtime;
    private final AtomicInteger tickCount = new AtomicInteger();
    private volatile TaskHandle repeatingHandle;

    @Override
    public void onLoad() {
        OperationResult<CommonRuntime> built = RuntimeBootstrap.build(this, builder ->
                builder.component(new DemoComponent())
        );
        if (built.isFailure()) {
            throw new IllegalStateException(built.errorOrNull().message(), built.errorOrNull().cause());
        }

        runtime = built.valueOrNull();

        OperationResult<Void> load = RuntimeBootstrap.safeLoad(runtime);
        if (load.isFailure()) {
            throw new IllegalStateException(load.errorOrNull().message(), load.errorOrNull().cause());
        }
    }

    @Override
    public void onEnable() {
        if (runtime == null) {
            throw new IllegalStateException("Runtime was not built during onLoad.");
        }
        OperationResult<Void> enable = RuntimeBootstrap.safeEnable(runtime);
        if (enable.isFailure()) {
            throw new IllegalStateException(enable.errorOrNull().message(), enable.errorOrNull().cause());
        }
    }

    @Override
    public void onDisable() {
        if (runtime == null) {
            return;
        }
        RuntimeBootstrap.safeDisable(runtime);
    }

    public CommonRuntime runtime() {
        return runtime;
    }

    public int tickCount() {
        return tickCount.get();
    }

    public TaskHandle repeatingHandle() {
        return repeatingHandle;
    }

    private final class DemoComponent implements CommonComponent {

        @Override
        public String id() {
            return "consumer-demo-component";
        }

        @Override
        public void onEnable(CommonContext context) {
            repeatingHandle = context.scheduler().runSyncRepeating(0L, 1L, tickCount::incrementAndGet);
        }
    }
}
