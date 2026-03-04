package dev.patric.commonlib.dialog;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogOpenRequest;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSession;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogDisableCleanupTest {

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
    void runtimeDisableClosesDialogSessionsAndCancelsTimeoutPath() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        DialogService service = runtime.services().require(DialogService.class);

        Player player = server.addPlayer();
        DialogSession session = service.open(new DialogOpenRequest(
                player.getUniqueId(),
                template("dialog.disable"),
                20L,
                Locale.ENGLISH,
                Map.of(),
                null
        ));

        assertTrue(service.find(session.sessionId()).isPresent());

        runtime.onDisable();
        assertFalse(service.find(session.sessionId()).isPresent());

        server.getScheduler().performTicks(30L);
        assertFalse(service.find(session.sessionId()).isPresent());
    }

    private static DialogTemplate template(String key) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("Disable"),
                        null,
                        true,
                        false,
                        DialogAfterAction.WAIT_FOR_RESPONSE,
                        List.of(),
                        List.of(new TextInputSpec("input", 120, Component.text("Input"), true, "", 20, null, null))
                ),
                new NoticeTypeSpec(new DialogButtonSpec(Component.text("Submit"), null, 100, new CustomActionSpec("submit", Map.of()))),
                Map.of()
        );
    }
}
