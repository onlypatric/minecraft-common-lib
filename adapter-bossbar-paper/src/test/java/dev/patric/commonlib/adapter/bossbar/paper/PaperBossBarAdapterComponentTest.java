package dev.patric.commonlib.adapter.bossbar.paper;

import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.port.BossBarPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaperBossBarAdapterComponentTest {

    @Test
    void componentBindsBossBarPortOnEnable() {
        PortBindingService bindingService = mock(PortBindingService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        CommonContext context = mock(CommonContext.class);
        BossBarPort bossBarPort = mock(BossBarPort.class);

        when(serviceRegistry.require(PortBindingService.class)).thenReturn(bindingService);
        when(context.services()).thenReturn(serviceRegistry);

        PaperBossBarAdapterComponent component = new PaperBossBarAdapterComponent(() -> bossBarPort);

        component.onEnable(context);

        verify(bindingService).bindBossBarPort(bossBarPort, "paper-bossbar", PaperBossBarAdapterComponent.BACKEND_VERSION);
    }
}
