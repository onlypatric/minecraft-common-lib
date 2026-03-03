package dev.patric.commonlib;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
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
    }
}
