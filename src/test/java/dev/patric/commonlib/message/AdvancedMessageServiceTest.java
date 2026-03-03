package dev.patric.commonlib.message;

import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.api.MessageService;
import dev.patric.commonlib.api.message.MessageRequest;
import dev.patric.commonlib.api.message.PlaceholderResolver;
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

class AdvancedMessageServiceTest {

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
    void localeFallbackPluralAndResolverChainWork() throws IOException {
        CommonRuntime runtime = CommonRuntime.builder(plugin).build();
        runtime.onLoad();

        ConfigService configs = runtime.services().require(ConfigService.class);
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        Files.writeString(messagesFile.toPath(), String.join("\n",
                "default.commonlib.bootstrap: \"Boot\"",
                "default.welcome: \"Hello <name> from <server>\"",
                "en.points.one: \"<name> has <count> point\"",
                "en.points.other: \"<name> has <count> points\""
        ));
        configs.reloadAll();

        MessageService messages = runtime.services().require(MessageService.class);
        messages.registerResolver(new PlaceholderResolver() {
            @Override
            public boolean supports(String placeholderKey) {
                return "server".equals(placeholderKey);
            }

            @Override
            public String resolve(String placeholderKey, MessageRequest request) {
                return "Paper";
            }
        });

        String fallback = PlainTextComponentSerializer.plainText().serialize(
                messages.render("welcome", Map.of("name", "Patric"), Locale.ITALIAN)
        );
        String one = PlainTextComponentSerializer.plainText().serialize(
                messages.render(new MessageRequest("points", Locale.ENGLISH, Map.of("name", "Patric"), 1L, Map.of()))
        );
        String other = PlainTextComponentSerializer.plainText().serialize(
                messages.render(new MessageRequest("points", Locale.ENGLISH, Map.of("name", "Patric"), 3L, Map.of()))
        );

        assertTrue(fallback.contains("Hello Patric from Paper"));
        assertEquals("Patric has 1 point", one);
        assertEquals("Patric has 3 points", other);
    }
}
