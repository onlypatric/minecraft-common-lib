package dev.patric.commonlib.policy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreDependencyPolicyTest {

    @Test
    void buildScriptContainsCoreDependencyPolicyTaskAndCheckHook() throws IOException {
        String script = Files.readString(Path.of("build.gradle.kts"));

        assertTrue(script.contains("verifyCoreDependencyPolicy"));
        assertTrue(script.contains("tasks.check"));
        assertTrue(script.contains("compileOnly"));
        assertTrue(script.contains("io.papermc.paper:paper-api"));
        assertTrue(script.contains("api/capability"));
        assertTrue(script.contains("api/hud"));
        assertTrue(script.contains("api/gui"));
        assertTrue(script.contains("api/port/noop"));
    }
}
