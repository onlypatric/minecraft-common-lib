package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiSessionTimeoutTest {

    private ServerMock server;
    private TestPlugin plugin;
    private CommonRuntime runtime;
    private GuiSessionService service;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        service = runtime.services().require(GuiSessionService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void sessionIsClosedOnTimeout() {
        UUID playerId = UUID.randomUUID();
        GuiSession opened = service.open(new GuiOpenRequest(playerId, "menu.timeout", null, 2L));

        assertTrue(service.find(opened.sessionId()).isPresent());

        server.getScheduler().performTicks(1L);
        assertTrue(service.find(opened.sessionId()).isPresent());

        server.getScheduler().performTicks(2L);
        assertFalse(service.find(opened.sessionId()).isPresent());
    }

    @Test
    void manualCloseCancelsTimeoutPath() {
        UUID playerId = UUID.randomUUID();
        GuiSession opened = service.open(new GuiOpenRequest(playerId, "menu.manual", null, 5L));

        assertTrue(service.close(opened.sessionId(), GuiCloseReason.USER_CLOSE));
        assertFalse(service.close(opened.sessionId(), GuiCloseReason.USER_CLOSE));

        server.getScheduler().performTicks(8L);
        assertFalse(service.find(opened.sessionId()).isPresent());
    }
}
