package dev.patric.commonlib.adapter.invui;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvUiAdapterComponentTest {

    @Test
    void componentMarksUnavailableWhenProbeFails() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        InvUiAdapterComponent component = new InvUiAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.unavailable("missing-class:xyz.xenondevs.invui.InvUI"),
                InvUiGuiPort::new
        );

        component.onEnable(context);

        verify(bindingService).markUnavailable(StandardCapabilities.GUI, "missing-class:xyz.xenondevs.invui.InvUI");
    }

    @Test
    void componentBindsGuiPortWhenDependencyIsAvailable() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        GuiPort guiPort = mock(GuiPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        InvUiAdapterComponent component = new InvUiAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.available("1.0.0"),
                () -> guiPort
        );

        component.onEnable(context);

        verify(bindingService).bindGuiPort(guiPort, "invui", "1.0.0");
    }
}
