package dev.patric.consumerdemo;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.TaskHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoConsumerPluginTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pluginLoadsRuntimeAndCancelsTasksOnDisable() {
        DemoConsumerPlugin plugin = MockBukkit.load(DemoConsumerPlugin.class);

        CommonRuntime runtime = plugin.runtime();
        assertNotNull(runtime);

        ServiceRegistry services = runtime.services();
        assertNotNull(services.require(CommonScheduler.class));
        assertNotNull(services.require(ServiceRegistry.class));

        server.getScheduler().performTicks(4L);
        assertTrue(plugin.tickCount() > 0);

        TaskHandle handle = plugin.repeatingHandle();
        assertNotNull(handle);

        server.getPluginManager().disablePlugin(plugin);
        assertTrue(handle.isCancelled());

        int ticksBefore = plugin.tickCount();
        server.getScheduler().performTicks(4L);
        assertEquals(ticksBefore, plugin.tickCount());
    }
}
