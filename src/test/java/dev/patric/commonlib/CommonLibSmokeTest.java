package dev.patric.commonlib;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandRegistry;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiEvent;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.message.FallbackChain;
import dev.patric.commonlib.api.message.MessageRequest;
import dev.patric.commonlib.api.message.PlaceholderResolver;
import dev.patric.commonlib.api.message.PluralRules;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopGuiPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.plugin.PluginLifecycle;
import dev.patric.commonlib.scheduler.Tasks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonLibSmokeTest {

    @Test
    void classesAreLoadableAndVersionIsPresent() {
        assertNotNull(CommonLib.version());
        assertDoesNotThrow(() -> Class.forName(CommonRuntime.class.getName()));
        assertDoesNotThrow(() -> Class.forName(PluginLifecycle.class.getName()));
        assertDoesNotThrow(() -> Class.forName(Tasks.class.getName()));
        assertDoesNotThrow(() -> Class.forName(CommandPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(ScoreboardPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(ArenaResetPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NpcPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(HologramPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(ClaimsPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(SchematicPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(PasteOptions.class.getName()));
        assertDoesNotThrow(() -> Class.forName(CapabilityRegistry.class.getName()));
        assertDoesNotThrow(() -> Class.forName(CapabilityStatus.class.getName()));
        assertDoesNotThrow(() -> Class.forName(StandardCapabilities.class.getName()));
        assertDoesNotThrow(() -> Class.forName(CommandModel.class.getName()));
        assertDoesNotThrow(() -> Class.forName(CommandRegistry.class.getName()));
        assertDoesNotThrow(() -> Class.forName(MessageRequest.class.getName()));
        assertDoesNotThrow(() -> Class.forName(PlaceholderResolver.class.getName()));
        assertDoesNotThrow(() -> Class.forName(FallbackChain.class.getName()));
        assertDoesNotThrow(() -> Class.forName(PluralRules.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiState.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiOpenRequest.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiSession.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiEvent.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiCloseReason.class.getName()));
        assertDoesNotThrow(() -> Class.forName(GuiSessionService.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NoopNpcPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NoopHologramPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NoopClaimsPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NoopSchematicPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NoopCommandPort.class.getName()));
        assertDoesNotThrow(() -> Class.forName(NoopGuiPort.class.getName()));
    }
}
