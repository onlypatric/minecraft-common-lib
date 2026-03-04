package dev.patric.commonlib.adapter.worldedit;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorldEditAdapterComponentTest {

    @Test
    void componentMarksUnavailableWhenProbeFails() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        WorldEditAdapterComponent component = new WorldEditAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.unavailable("missing-plugin:WorldEdit"),
                WorldEditSchematicPort::new
        );

        component.onEnable(context);

        verify(bindingService).markUnavailable(StandardCapabilities.SCHEMATIC, "missing-plugin:WorldEdit");
    }

    @Test
    void componentBindsWhenProbeIsAvailable() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        SchematicPort schematicPort = mock(SchematicPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        WorldEditAdapterComponent component = new WorldEditAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.available("7.3.0"),
                () -> schematicPort
        );

        component.onEnable(context);

        verify(bindingService).bindSchematicPort(schematicPort, "worldedit", "7.3.0");
    }
}
