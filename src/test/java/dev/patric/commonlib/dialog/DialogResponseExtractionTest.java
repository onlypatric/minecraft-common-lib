package dev.patric.commonlib.dialog;

import dev.patric.commonlib.api.dialog.BooleanInputSpec;
import dev.patric.commonlib.api.dialog.DialogInputSpec;
import dev.patric.commonlib.api.dialog.NumberRangeInputSpec;
import dev.patric.commonlib.api.dialog.SingleOptionEntrySpec;
import dev.patric.commonlib.api.dialog.SingleOptionInputSpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.runtime.dialog.DefaultDialogResponse;
import io.papermc.paper.dialog.DialogResponseView;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogResponseExtractionTest {

    @Test
    void responseSupportsTypedReadsAndMapExtraction() {
        List<DialogInputSpec> inputSpecs = List.of(
                new TextInputSpec("name", 120, Component.text("Name"), true, "", 64, null, null),
                new BooleanInputSpec("ready", Component.text("Ready"), false, "yes", "no"),
                new NumberRangeInputSpec("amount", 100, Component.text("Amount"), "%s", 0f, 100f, 10f, 1f),
                new SingleOptionInputSpec("team", 120, Component.text("Team"), true, List.of(
                        new SingleOptionEntrySpec("red", Component.text("Red"), true),
                        new SingleOptionEntrySpec("blue", Component.text("Blue"), false)
                ))
        );

        DefaultDialogResponse response = new DefaultDialogResponse(new FixedResponseView(), inputSpecs);

        assertEquals("alex", response.text("name").orElseThrow());
        assertTrue(response.bool("ready").orElseThrow());
        assertEquals(42f, response.number("amount").orElseThrow());
        assertEquals("{name:\"alex\",ready:1b,amount:42f,team:\"red\"}", response.rawPayload());

        Map<String, Object> extracted = response.asMap();
        assertEquals("alex", extracted.get("name"));
        assertEquals(true, extracted.get("ready"));
        assertEquals(42f, extracted.get("amount"));
        assertEquals("red", extracted.get("team"));
        assertTrue(extracted.containsKey("_raw"));
        assertFalse(response.text("missing").isPresent());
    }

    private static final class FixedResponseView implements DialogResponseView {

        @Override
        public BinaryTagHolder payload() {
            return BinaryTagHolder.binaryTagHolder("{name:\"alex\",ready:1b,amount:42f,team:\"red\"}");
        }

        @Override
        public String getText(String key) {
            return switch (key) {
                case "name" -> "alex";
                case "team" -> "red";
                default -> null;
            };
        }

        @Override
        public Boolean getBoolean(String key) {
            if ("ready".equals(key)) {
                return true;
            }
            return null;
        }

        @Override
        public Float getFloat(String key) {
            if ("amount".equals(key)) {
                return 42f;
            }
            return null;
        }
    }
}
