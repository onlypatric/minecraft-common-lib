package dev.patric.commonlib.module;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.module.CommonModule;
import dev.patric.commonlib.api.module.ModuleRegistry;
import dev.patric.commonlib.api.module.ModuleState;
import dev.patric.commonlib.testsupport.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleRegistryStateTransitionTest {

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
    void registryTracksRegisteredEnabledAndDisabledTransitions() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .module(new CommonModule() {
                    @Override
                    public String id() {
                        return "core-services";
                    }
                })
                .build();

        ModuleRegistry registry = runtime.services().require(ModuleRegistry.class);
        assertEquals(ModuleState.REGISTERED, registry.find("core-services").orElseThrow().state());

        runtime.onLoad();
        assertEquals(ModuleState.REGISTERED, registry.find("core-services").orElseThrow().state());

        runtime.onEnable();
        assertEquals(ModuleState.ENABLED, registry.find("core-services").orElseThrow().state());
        assertTrue(registry.isEnabled("core-services"));

        runtime.onDisable();
        assertEquals(ModuleState.DISABLED, registry.find("core-services").orElseThrow().state());
    }
}
