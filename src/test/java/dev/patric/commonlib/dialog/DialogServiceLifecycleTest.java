package dev.patric.commonlib.dialog;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogCloseReason;
import dev.patric.commonlib.api.dialog.DialogOpenRequest;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSession;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.api.dialog.PlainMessageBodySpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogServiceLifecycleTest {

    private ServerMock server;
    private TestPlugin plugin;
    private CommonRuntime runtime;
    private DialogService service;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        service = runtime.services().require(DialogService.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void openFindCloseAndTimeoutFlowWorks() {
        Player player = server.addPlayer();
        DialogTemplate template = simpleTemplate("dialog.lifecycle");

        DialogSession opened = service.open(new DialogOpenRequest(
                player.getUniqueId(),
                template,
                2L,
                Locale.ENGLISH,
                Map.of(),
                null
        ));

        assertTrue(service.find(opened.sessionId()).isPresent());
        assertTrue(service.activeByPlayer(player.getUniqueId()).stream().anyMatch(it -> it.sessionId().equals(opened.sessionId())));

        server.getScheduler().performTicks(3L);
        assertFalse(service.find(opened.sessionId()).isPresent());

        DialogSession opened2 = service.open(new DialogOpenRequest(
                player.getUniqueId(),
                simpleTemplate("dialog.lifecycle.manual"),
                0L,
                Locale.ENGLISH,
                Map.of(),
                null
        ));
        assertTrue(service.close(opened2.sessionId(), DialogCloseReason.USER_CLOSE));
        assertFalse(service.close(opened2.sessionId(), DialogCloseReason.USER_CLOSE));
    }

    @Test
    void closeAllByPlayerAndCloseAllWork() {
        Player p1 = server.addPlayer("p1");
        Player p2 = server.addPlayer("p2");

        DialogSession s1 = service.open(new DialogOpenRequest(p1.getUniqueId(), simpleTemplate("dialog.batch.1"), 0L, Locale.ENGLISH, Map.of(), null));
        DialogSession s2 = service.open(new DialogOpenRequest(p1.getUniqueId(), simpleTemplate("dialog.batch.2"), 0L, Locale.ENGLISH, Map.of(), null));
        DialogSession s3 = service.open(new DialogOpenRequest(p2.getUniqueId(), simpleTemplate("dialog.batch.3"), 0L, Locale.ENGLISH, Map.of(), null));

        int closedForP1 = service.closeAllByPlayer(p1.getUniqueId(), DialogCloseReason.USER_CLOSE);
        assertTrue(closedForP1 >= 1);
        assertFalse(service.find(s1.sessionId()).isPresent());
        assertFalse(service.find(s2.sessionId()).isPresent());
        assertTrue(service.find(s3.sessionId()).isPresent());

        int closedRemaining = service.closeAll(DialogCloseReason.PLUGIN_DISABLE);
        assertTrue(closedRemaining >= 1);
        assertFalse(service.find(s3.sessionId()).isPresent());
    }

    private static DialogTemplate simpleTemplate(String key) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("Dialog"),
                        null,
                        true,
                        false,
                        DialogAfterAction.WAIT_FOR_RESPONSE,
                        List.of(new PlainMessageBodySpec(Component.text("Body"), 160)),
                        List.of(new TextInputSpec("message", 160, Component.text("Message"), true, "", 64, null, null))
                ),
                new NoticeTypeSpec(
                        new DialogButtonSpec(
                                Component.text("Submit"),
                                null,
                                120,
                                new CustomActionSpec("submit", Map.of())
                        )
                ),
                Map.of()
        );
    }
}
