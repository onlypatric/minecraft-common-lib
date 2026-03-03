package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.bootstrap.RuntimeBootstrap;
import dev.patric.commonlib.api.error.OperationResult;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RcRuntimeBootstrapSmokeTest {

    private ServerMock server;
    private TestPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void runtimeBootstrapLifecycleCompletesAndDisablesCleanly() {
        AtomicInteger repeatingTicks = new AtomicInteger();
        AtomicReference<TaskHandle> handleRef = new AtomicReference<>();

        OperationResult<CommonRuntime> built = RuntimeBootstrap.build(plugin, builder ->
                builder.component(new CommonComponent() {
                    @Override
                    public String id() {
                        return "rc-bootstrap-smoke";
                    }

                    @Override
                    public void onEnable(CommonContext context) {
                        handleRef.set(context.scheduler().runSyncRepeating(0L, 1L, repeatingTicks::incrementAndGet));
                    }
                })
        );

        assertTrue(built.isSuccess());
        CommonRuntime runtime = built.valueOrNull();
        assertNotNull(runtime);

        assertTrue(RuntimeBootstrap.safeLoad(runtime).isSuccess());
        assertTrue(RuntimeBootstrap.safeEnable(runtime).isSuccess());

        server.getScheduler().performTicks(4L);
        assertTrue(repeatingTicks.get() > 0);

        assertTrue(RuntimeBootstrap.safeDisable(runtime).isSuccess());
        TaskHandle handle = handleRef.get();
        assertNotNull(handle);
        assertTrue(handle.isCancelled());

        int ticksBefore = repeatingTicks.get();
        server.getScheduler().performTicks(4L);
        assertEquals(ticksBefore, repeatingTicks.get());
    }
}
