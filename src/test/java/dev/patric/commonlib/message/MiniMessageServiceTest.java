package dev.patric.commonlib.message;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.api.MessageService;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniMessageServiceTest {

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
    void localeAndPlaceholderFallbackWorks() throws IOException {
        CommonRuntime runtime = CommonRuntime.builder(plugin).build();
        runtime.onLoad();

        ConfigService configs = runtime.services().require(ConfigService.class);
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        Files.writeString(messagesFile.toPath(), String.join("\n",
                "default.commonlib.bootstrap: \"Boot\"",
                "default.hello: \"Hello <name>\"",
                "it.hello: \"Ciao <name>\""
        ));
        configs.reloadAll();

        MessageService messages = runtime.services().require(MessageService.class);
        String italian = PlainTextComponentSerializer.plainText().serialize(
                messages.render("hello", Map.of("name", "Patric"), Locale.ITALIAN)
        );
        String fallback = PlainTextComponentSerializer.plainText().serialize(messages.render("missing-key"));

        assertEquals("Ciao Patric", italian);
        assertTrue(fallback.contains("Missing message key"));
    }
}
