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
        assertTrue(script.contains("api/adapter"));
        assertTrue(script.contains("api/capability"));
        assertTrue(script.contains("api/packet"));
        assertTrue(script.contains("api/match"));
        assertTrue(script.contains("api/hud"));
        assertTrue(script.contains("api/gui"));
        assertTrue(script.contains("api/arena"));
        assertTrue(script.contains("api/team"));
        assertTrue(script.contains("api/persistence"));
        assertTrue(script.contains("api/port/noop"));
        assertTrue(script.contains("integrationTest"));
        assertTrue(script.contains("adapterIntegrationTest"));
        assertTrue(script.contains("externalMatrixTest"));
        assertTrue(script.contains("runIntegrationHarness"));
        assertTrue(script.contains("runAdapterIntegration"));
        assertTrue(script.contains("runExternalMatrix"));
        assertTrue(script.contains("verifyAdapterDependencyPolicy"));
        assertTrue(script.contains("verifyAdapterLicensePolicy"));
        assertTrue(script.contains(":adapter-huskclaims"));
        assertTrue(script.contains(":adapter-worldedit"));
        assertTrue(script.contains(":adapter-fawe"));
        assertTrue(script.contains(":adapter-bossbar-paper"));
        assertTrue(script.contains(":adapter-bstats"));
        assertTrue(script.contains(":adapter-protocollib"));
    }
}
