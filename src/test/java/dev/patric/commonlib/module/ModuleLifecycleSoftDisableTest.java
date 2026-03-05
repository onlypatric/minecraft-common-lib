package dev.patric.commonlib.module;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModuleRegistry;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ModuleLifecycleSoftDisableTest {

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
    void runtimeContinuesWhenModuleFailsEnable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .module(new BaseModule("core"))
                .module(new FailingEnableModule("broken", Set.of("core")))
                .build();

        assertDoesNotThrow(runtime::onLoad);
        assertDoesNotThrow(runtime::onEnable);

        ModuleRegistry registry = runtime.services().require(ModuleRegistry.class);
        assertEquals(ModuleState.ENABLED, registry.find("core").orElseThrow().state());
        assertEquals(ModuleState.FAILED_ENABLE, registry.find("broken").orElseThrow().state());

        runtime.onDisable();
        assertEquals(ModuleState.DISABLED, registry.find("core").orElseThrow().state());
        assertEquals(ModuleState.FAILED_ENABLE, registry.find("broken").orElseThrow().state());
    }

    private record BaseModule(String id) implements CommonModule {
    }

    private record FailingEnableModule(String id, Set<String> dependsOn) implements CommonModule {

        @Override
        public void onEnable(CommonContext ctx) {
            throw new IllegalStateException("boom");
        }
    }
}
