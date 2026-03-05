package dev.patric.commonlib.module;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModuleRegistry;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleDependencySkipPropagationTest {

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
    void dependentModulesAreSoftSkippedWhenParentIsInactive() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .module(new FailingLoadModule("a"))
                .module(new BaseModule("b", Set.of("a")))
                .module(new BaseModule("c", Set.of("b")))
                .build();

        runtime.onLoad();
        runtime.onEnable();

        ModuleRegistry registry = runtime.services().require(ModuleRegistry.class);
        assertEquals(ModuleState.FAILED_LOAD, registry.find("a").orElseThrow().state());
        assertEquals(ModuleState.SKIPPED_DEPENDENCY_INACTIVE, registry.find("b").orElseThrow().state());
        assertEquals(ModuleState.SKIPPED_DEPENDENCY_INACTIVE, registry.find("c").orElseThrow().state());
    }

    private record BaseModule(String id, Set<String> dependsOn) implements CommonModule {
    }

    private record FailingLoadModule(String id) implements CommonModule {

        @Override
        public Set<String> dependsOn() {
            return Set.of();
        }

        @Override
        public void onLoad(CommonContext ctx) {
            throw new IllegalStateException("load fail");
        }
    }
}
