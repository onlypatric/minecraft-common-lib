package dev.patric.commonlib.module;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleAndComponentCoexistenceTest {

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
    void modulesAndComponentsUseExpectedLifecycleOrder() {
        List<String> calls = new ArrayList<>();

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .module(new OrderedModule("module-core", calls))
                .component(new OrderedComponent("component-core", calls))
                .build();

        runtime.onLoad();
        runtime.onEnable();
        runtime.onDisable();

        assertEquals(List.of(
                "load:module-core",
                "load:component-core",
                "enable:module-core",
                "enable:component-core",
                "disable:component-core",
                "disable:module-core"
        ), calls);
    }

    private record OrderedModule(String id, List<String> calls) implements CommonModule {

        @Override
        public void onLoad(CommonContext ctx) {
            calls.add("load:" + id);
        }

        @Override
        public void onEnable(CommonContext ctx) {
            calls.add("enable:" + id);
        }

        @Override
        public void onDisable(CommonContext ctx) {
            calls.add("disable:" + id);
        }
    }

    private record OrderedComponent(String id, List<String> calls) implements CommonComponent {

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
}
