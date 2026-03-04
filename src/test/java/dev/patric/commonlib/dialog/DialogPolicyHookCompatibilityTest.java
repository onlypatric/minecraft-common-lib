package dev.patric.commonlib.dialog;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogCloseReason;
import dev.patric.commonlib.api.dialog.DialogEventResult;
import dev.patric.commonlib.api.dialog.DialogOpenRequest;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSession;
import dev.patric.commonlib.api.dialog.DialogSubmission;
import dev.patric.commonlib.api.dialog.DialogSubmitEvent;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.api.dialog.PlainMessageBodySpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.guard.PolicyDecision;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogPolicyHookCompatibilityTest {

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
    void denyPolicyBlocksSubmitEvent() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        DialogService service = runtime.services().require(DialogService.class);
        EventRouter router = runtime.services().require(EventRouter.class);

        router.registerPolicy(dev.patric.commonlib.lifecycle.dialog.DialogPolicyRoutedEvent.class, event -> PolicyDecision.deny("blocked"));

        Player player = server.addPlayer();
        DialogSession session = service.open(new DialogOpenRequest(player.getUniqueId(), template("dialog.policy.deny"), 0L, Locale.ENGLISH, Map.of(), null));
        DialogSubmission submission = new DialogSubmission(
                session.sessionId(),
                player.getUniqueId(),
                "submit",
                new dev.patric.commonlib.runtime.dialog.DefaultDialogResponse(new FakeResponseView(), List.of()),
                System.currentTimeMillis()
        );

        DialogEventResult result = service.publish(new DialogSubmitEvent(session.sessionId(), 0L, submission));
        assertEquals(DialogEventResult.DENIED_BY_POLICY, result);
        assertTrue(service.find(session.sessionId()).isPresent());
    }

    @Test
    void allowPolicyLetsSubmitCloseSession() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();
        DialogService service = runtime.services().require(DialogService.class);
        EventRouter router = runtime.services().require(EventRouter.class);

        router.registerPolicy(dev.patric.commonlib.lifecycle.dialog.DialogPolicyRoutedEvent.class, event -> PolicyDecision.allow());

        Player player = server.addPlayer();
        DialogSession session = service.open(new DialogOpenRequest(player.getUniqueId(), template("dialog.policy.allow"), 0L, Locale.ENGLISH, Map.of(), null));
        DialogSubmission submission = new DialogSubmission(
                session.sessionId(),
                player.getUniqueId(),
                "submit",
                new dev.patric.commonlib.runtime.dialog.DefaultDialogResponse(new FakeResponseView(), List.of()),
                System.currentTimeMillis()
        );

        DialogEventResult result = service.publish(new DialogSubmitEvent(session.sessionId(), 0L, submission));
        assertEquals(DialogEventResult.APPLIED, result);
        assertFalse(service.find(session.sessionId()).isPresent());
    }

    private static DialogTemplate template(String key) {
        return new DialogTemplate(
                key,
                new DialogBaseSpec(
                        Component.text("Policy"),
                        null,
                        true,
                        false,
                        DialogAfterAction.WAIT_FOR_RESPONSE,
                        List.of(new PlainMessageBodySpec(Component.text("Body"), 120)),
                        List.of(new TextInputSpec("message", 120, Component.text("Message"), true, "", 32, null, null))
                ),
                new NoticeTypeSpec(new DialogButtonSpec(Component.text("Submit"), null, 100, new CustomActionSpec("submit", Map.of()))),
                Map.of()
        );
    }

    private static final class FakeResponseView implements io.papermc.paper.dialog.DialogResponseView {

        @Override
        public BinaryTagHolder payload() {
            return BinaryTagHolder.binaryTagHolder("{message:\"ok\"}");
        }

        @Override
        public String getText(String key) {
            return "ok";
        }

        @Override
        public Boolean getBoolean(String key) {
            return null;
        }

        @Override
        public Float getFloat(String key) {
            return null;
        }
    }
}
