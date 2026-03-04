package dev.patric.commonlib.adapter.protocollib;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.PacketPort;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtocolLibAdapterComponentTest {

    @Test
    void componentMarksUnavailableWhenProbeFails() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        ProtocolLibAdapterComponent component = new ProtocolLibAdapterComponent(
                plugin -> ProtocolLibAdapterComponent.ProbeResult.unavailable("missing-plugin:ProtocolLib"),
                ProtocolLibPacketPort::new
        );

        component.onEnable(context);

        verify(bindingService).markUnavailable(StandardCapabilities.PACKETS, "missing-plugin:ProtocolLib");
    }

    @Test
    void componentBindsPacketPortWhenProbeIsAvailable() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        PacketPort packetPort = mock(PacketPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        ProtocolLibAdapterComponent component = new ProtocolLibAdapterComponent(
                plugin -> ProtocolLibAdapterComponent.ProbeResult.available("5.3.0-SNAPSHOT"),
                () -> packetPort
        );

        component.onEnable(context);

        verify(bindingService).bindPacketPort(packetPort, "protocollib", "5.3.0-SNAPSHOT");
    }
}
