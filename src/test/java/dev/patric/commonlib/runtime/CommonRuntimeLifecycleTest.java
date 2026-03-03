package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonRuntimeLifecycleTest {

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
    void lifecycleOrderIsDeterministic() {
        List<String> calls = new ArrayList<>();

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .component(new RecordingComponent("alpha", calls))
                .component(new RecordingComponent("beta", calls))
                .build();

        runtime.onLoad();
        runtime.onEnable();
        runtime.onDisable();

        assertEquals(List.of(
                "load:alpha",
                "load:beta",
                "enable:alpha",
                "enable:beta",
                "disable:beta",
                "disable:alpha"
        ), calls);
    }

    @Test
    void enableFailureRollsBackEnabledComponents() {
        List<String> calls = new ArrayList<>();

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .component(new RecordingComponent("first", calls))
                .component(new FailingEnableComponent("boom", calls))
                .component(new RecordingComponent("never", calls))
                .build();

        runtime.onLoad();
        assertThrows(IllegalStateException.class, runtime::onEnable);

        assertEquals(List.of(
                "load:first",
                "load:boom",
                "load:never",
                "enable:first",
                "enable:boom",
                "disable:first"
        ), calls);
    }

    private record RecordingComponent(String id, List<String> calls) implements CommonComponent {

        @Override
        public void onLoad(CommonContext context) {
            calls.add("load:" + id);
        }

        @Override
        public void onEnable(CommonContext context) {
            calls.add("enable:" + id);
        }

        @Override
        public void onDisable(CommonContext context) {
            calls.add("disable:" + id);
        }
    }

    private record FailingEnableComponent(String id, List<String> calls) implements CommonComponent {

        @Override
        public void onLoad(CommonContext context) {
            calls.add("load:" + id);
        }

        @Override
        public void onEnable(CommonContext context) {
            calls.add("enable:" + id);
            throw new IllegalStateException("forced enable failure");
        }

        @Override
        public void onDisable(CommonContext context) {
            calls.add("disable:" + id);
        }
    }
}
