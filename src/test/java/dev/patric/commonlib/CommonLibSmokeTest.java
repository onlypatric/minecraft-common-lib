package dev.patric.commonlib;

import dev.patric.commonlib.plugin.PluginLifecycle;
import dev.patric.commonlib.scheduler.Tasks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonLibSmokeTest {

    @Test
    void classesAreLoadableAndVersionIsPresent() {
        assertNotNull(CommonLib.version());
        assertDoesNotThrow(() -> Class.forName(PluginLifecycle.class.getName()));
        assertDoesNotThrow(() -> Class.forName(Tasks.class.getName()));
    }
}
