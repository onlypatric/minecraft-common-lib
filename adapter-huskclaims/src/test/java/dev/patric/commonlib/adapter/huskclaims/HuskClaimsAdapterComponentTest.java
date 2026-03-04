package dev.patric.commonlib.adapter.huskclaims;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HuskClaimsAdapterComponentTest {

    @Test
    void componentMarksUnavailableWhenProbeFails() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        HuskClaimsAdapterComponent component = new HuskClaimsAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.unavailable("missing-plugin:HuskClaims"),
                HuskClaimsClaimsPort::new
        );

        component.onEnable(context);

        verify(bindingService).markUnavailable(StandardCapabilities.CLAIMS, "missing-plugin:HuskClaims");
    }

    @Test
    void componentBindsClaimsPortWhenDependencyIsAvailable() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        ClaimsPort claimsPort = mock(ClaimsPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);
        when(context.plugin()).thenReturn(mock(JavaPlugin.class));

        HuskClaimsAdapterComponent component = new HuskClaimsAdapterComponent(
                plugin -> BukkitDependencyProbe.ProbeResult.available("4.7.1"),
                () -> claimsPort
        );

        component.onEnable(context);

        verify(bindingService).bindClaimsPort(claimsPort, "huskclaims", "4.7.1");
    }
}
