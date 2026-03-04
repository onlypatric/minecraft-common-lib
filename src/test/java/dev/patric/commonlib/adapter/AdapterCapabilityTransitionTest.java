package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterCapabilityTransitionTest {

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
    void bindingTransitionsCapabilityAndRestoresFallbackOnUnavailable() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        PortBindingService bindingService = runtime.services().require(PortBindingService.class);
        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);
        CommandPort commandPort = runtime.services().require(CommandPort.class);

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertFalse(commandPort.supportsSuggestions());

        bindingService.bindCommandPort(new SuggestingCommandPort(), "CommandAPI", "11.1.0");

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertEquals("commandapi:11.1.0", capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().metadata());
        assertTrue(commandPort.supportsSuggestions());

        bindingService.markUnavailable(StandardCapabilities.COMMAND, "disabled-plugin:CommandAPI");

        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertEquals(
                "disabled-plugin:CommandAPI",
                capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().reason()
        );
        assertFalse(commandPort.supportsSuggestions());
    }

    @Test
    void scoreboardBindingTransitionsCapabilityAndFallback() {
        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .includeDefaultCoreComponents(false)
                .build();

        PortBindingService bindingService = runtime.services().require(PortBindingService.class);
        CapabilityRegistry capabilityRegistry = runtime.services().require(CapabilityRegistry.class);
        ScoreboardPort scoreboardPort = runtime.services().require(ScoreboardPort.class);

        ScoreboardSession session = new ScoreboardSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main",
                new ScoreboardSnapshot("Title", List.of("L1")),
                ScoreboardSessionStatus.OPEN,
                System.currentTimeMillis(),
                0L
        );

        assertTrue(scoreboardPort.open(session));
        bindingService.bindScoreboardPort(new RejectingScoreboardPort(), "fastboard", "2.1.5");

        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.SCOREBOARD));
        assertFalse(scoreboardPort.open(session));

        bindingService.markUnavailable(StandardCapabilities.SCOREBOARD, "missing-class:fr.mrmicky.fastboard.FastBoard");
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.SCOREBOARD));
        assertTrue(scoreboardPort.open(session));
    }

    private static final class SuggestingCommandPort implements CommandPort {

        @Override
        public void register(CommandModel model) {
            // no-op
        }

        @Override
        public void unregister(String root) {
            // no-op
        }

        @Override
        public boolean supportsSuggestions() {
            return true;
        }
    }

    private static final class RejectingScoreboardPort implements ScoreboardPort {

        @Override
        public boolean open(ScoreboardSession session) {
            return false;
        }

        @Override
        public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
            return false;
        }

        @Override
        public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
            return false;
        }
    }
}
