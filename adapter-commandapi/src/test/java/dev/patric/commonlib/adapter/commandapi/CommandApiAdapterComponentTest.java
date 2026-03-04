package dev.patric.commonlib.adapter.commandapi;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandApiAdapterComponentTest {

    @Test
    void componentMarksUnavailableWhenProbeFails() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        CommandApiAdapterComponent component = new CommandApiAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.unavailable("missing-plugin:CommandAPI"),
                CommandApiCommandPort::new
        );

        component.onEnable(context);

        verify(bindingService).markUnavailable(StandardCapabilities.COMMAND, "missing-plugin:CommandAPI");
    }

    @Test
    void componentBindsCommandPortWhenDependencyIsAvailable() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        CommandPort commandPort = mock(CommandPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        CommandApiAdapterComponent component = new CommandApiAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.available("11.1.0"),
                () -> commandPort
        );

        component.onEnable(context);

        verify(bindingService).bindCommandPort(commandPort, "commandapi", "11.1.0");
    }
}
