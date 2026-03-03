package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.GuiClickEvent;
import dev.patric.commonlib.api.gui.GuiEventResult;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.lifecycle.gui.GuiPolicyRoutedEvent;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiPolicyHookCompatibilityTest {

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
    void denyPolicyBlocksPortableGuiEvent() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        GuiSessionService service = runtime.services().require(GuiSessionService.class);
        EventRouter router = runtime.services().require(EventRouter.class);

        router.registerPolicy(GuiPolicyRoutedEvent.class, event -> PolicyDecision.deny("blocked"));

        GuiSession opened = service.open(new GuiOpenRequest(UUID.randomUUID(), "menu.policy", null, 0L));
        GuiEventResult result = service.publish(new GuiClickEvent(opened.sessionId(), opened.state().revision(), ClickAction.LEFT, 5));

        assertEquals(GuiEventResult.DENIED_BY_POLICY, result);
        assertTrue(service.find(opened.sessionId()).isPresent());
    }

    @Test
    void allowPolicyKeepsEventPathOperational() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        GuiSessionService service = runtime.services().require(GuiSessionService.class);
        EventRouter router = runtime.services().require(EventRouter.class);

        router.registerPolicy(GuiPolicyRoutedEvent.class, event -> PolicyDecision.allow());

        GuiSession opened = service.open(new GuiOpenRequest(UUID.randomUUID(), "menu.policy.allow", null, 0L));
        GuiEventResult result = service.publish(new GuiClickEvent(opened.sessionId(), opened.state().revision(), ClickAction.RIGHT, 3));

        assertEquals(GuiEventResult.APPLIED, result);
    }
}
