package dev.patric.commonlib.gui;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.GuiCloseEvent;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDisconnectEvent;
import dev.patric.commonlib.api.gui.GuiEventResult;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.GuiClickEvent;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiEventConcurrencyTest {

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
    void concurrentClickCloseDisconnectAreRaceSafe() throws Exception {
        UUID playerId = UUID.randomUUID();

        for (int i = 0; i < 25; i++) {
            GuiSession opened = service.open(new GuiOpenRequest(playerId, "menu.race", null, 0L));
            long revision = opened.state().revision();

            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(3);
            try {
                Callable<GuiEventResult> clickTask = () -> {
                    start.await();
                    return service.publish(new GuiClickEvent(opened.sessionId(), revision, ClickAction.LEFT, 0));
                };
                Callable<GuiEventResult> closeTask = () -> {
                    start.await();
                    return service.publish(new GuiCloseEvent(opened.sessionId(), revision, GuiCloseReason.USER_CLOSE));
                };
                Callable<GuiEventResult> disconnectTask = () -> {
                    start.await();
                    return service.publish(new GuiDisconnectEvent(opened.sessionId(), revision, playerId));
                };

                Future<GuiEventResult> click = executor.submit(clickTask);
                Future<GuiEventResult> close = executor.submit(closeTask);
                Future<GuiEventResult> disconnect = executor.submit(disconnectTask);

                start.countDown();

                List<GuiEventResult> results = List.of(click.get(), close.get(), disconnect.get());
                for (GuiEventResult result : results) {
                    assertTrue(result == GuiEventResult.APPLIED
                                    || result == GuiEventResult.SESSION_NOT_OPEN
                                    || result == GuiEventResult.SESSION_NOT_FOUND,
                            "unexpected result: " + result);
                }
            } finally {
                executor.shutdownNow();
            }

            assertFalse(service.find(opened.sessionId()).isPresent());
        }
    }
}
