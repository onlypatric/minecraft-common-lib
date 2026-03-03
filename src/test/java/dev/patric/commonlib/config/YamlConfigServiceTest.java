package dev.patric.commonlib.config;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlConfigServiceTest {

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
    void reloadAndFallbackWorkForKnownFiles() throws IOException {
        CommonRuntime runtime = CommonRuntime.builder(plugin).build();
        runtime.onLoad();

        ConfigService configs = runtime.services().require(ConfigService.class);
        File mainFile = new File(plugin.getDataFolder(), "config.yml");
        Files.writeString(mainFile.toPath(), "sample: 42\n");

        configs.reloadAll();

        assertEquals(42, configs.main().getInt("sample"));

        FileConfiguration custom = configs.load("custom/settings.yml");
        assertNotNull(custom);
        assertTrue(new File(plugin.getDataFolder(), "custom/settings.yml").exists());
    }

    @Test
    void unsafePathIsRejected() {
        CommonRuntime runtime = CommonRuntime.builder(plugin).build();
        runtime.onLoad();

        ConfigService configs = runtime.services().require(ConfigService.class);
        assertThrows(IllegalArgumentException.class, () -> configs.load("../secrets.yml"));
    }
}
