package dev.patric.commonlib.runtime.dialog;

import dev.patric.commonlib.api.dialog.BooleanInputSpec;
import dev.patric.commonlib.api.dialog.DialogInputSpec;
import dev.patric.commonlib.api.dialog.DialogResponse;
import dev.patric.commonlib.api.dialog.NumberRangeInputSpec;
import dev.patric.commonlib.api.dialog.SingleOptionInputSpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import io.papermc.paper.dialog.DialogResponseView;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default typed dialog response implementation backed by Paper response view.
 */
public final class DefaultDialogResponse implements DialogResponse {

    private final DialogResponseView view;
    private final String payload;
    private final Map<String, Object> extracted;

    /**
     * Creates a typed response wrapper.
     *
     * @param view paper response view.
     * @param inputSpecs input specification list.
     */
    public DefaultDialogResponse(DialogResponseView view, List<DialogInputSpec> inputSpecs) {
        this.view = Objects.requireNonNull(view, "view");
        this.payload = view.payload().string();
        this.extracted = extract(view, inputSpecs == null ? List.of() : inputSpecs);
    }

    @Override
    public Optional<String> text(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(view.getText(key));
    }

    @Override
    public Optional<Boolean> bool(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(view.getBoolean(key));
    }

    @Override
    public Optional<Float> number(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(view.getFloat(key));
    }

    @Override
    public String rawPayload() {
        return payload;
    }

    @Override
    public Map<String, Object> asMap() {
        return extracted;
    }

    private static Map<String, Object> extract(DialogResponseView view, List<DialogInputSpec> inputSpecs) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (DialogInputSpec spec : inputSpecs) {
            if (spec instanceof TextInputSpec textSpec) {
                String value = view.getText(textSpec.key());
                if (value != null) {
                    values.put(textSpec.key(), value);
                }
                continue;
            }
            if (spec instanceof SingleOptionInputSpec singleOptionInputSpec) {
                String value = view.getText(singleOptionInputSpec.key());
                if (value != null) {
                    values.put(singleOptionInputSpec.key(), value);
                }
                continue;
            }
            if (spec instanceof BooleanInputSpec booleanSpec) {
                Boolean value = view.getBoolean(booleanSpec.key());
                if (value != null) {
                    values.put(booleanSpec.key(), value);
                }
                continue;
            }
            if (spec instanceof NumberRangeInputSpec numberSpec) {
                Float value = view.getFloat(numberSpec.key());
                if (value != null) {
                    values.put(numberSpec.key(), value);
                }
            }
        }
        values.put("_raw", view.payload().string());
        return Map.copyOf(values);
    }
}
