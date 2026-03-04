package dev.patric.commonlib.adapter.bstats;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.MetricsPort;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BStatsAdapterComponentTest {

    @Test
    void componentMarksUnavailableWhenPluginIdIsInvalid() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);

        BStatsAdapterComponent component = new BStatsAdapterComponent(0, BStatsMetricsPort::new);

        component.onEnable(context);

        verify(bindingService).markUnavailable(StandardCapabilities.METRICS, "binding-failed:bstats:invalid-plugin-id");
    }

    @Test
    void componentBindsWhenMetricsInitializationSucceeds() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        MetricsPort metricsPort = mock(MetricsPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));
        when(metricsPort.initialize(context.plugin(), 12345)).thenReturn(true);

        BStatsAdapterComponent component = new BStatsAdapterComponent(12345, () -> metricsPort);

        component.onEnable(context);

        verify(bindingService).bindMetricsPort(metricsPort, "bstats", BStatsAdapterComponent.BACKEND_VERSION);
    }
}
