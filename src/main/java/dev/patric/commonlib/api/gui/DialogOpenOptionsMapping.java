package dev.patric.commonlib.api.gui;

import java.util.Map;
import java.util.List;

/**
 * Mapping options used when opening dialogs from GUI actions.
 *
 * @param includeGuiState whether GUI state keys are copied as placeholders.
 * @param includeGuiPlaceholders whether GUI open placeholders are copied.
 * @param staticPlaceholders static placeholders to inject.
 * @param responseBindings response bindings applied to GUI state on submit.
 */
public record DialogOpenOptionsMapping(
        boolean includeGuiState,
        boolean includeGuiPlaceholders,
        Map<String, String> staticPlaceholders,
        List<DialogResponseBinding> responseBindings
) {

    /**
     * Compact constructor validation.
     */
    public DialogOpenOptionsMapping {
        staticPlaceholders = Map.copyOf(staticPlaceholders == null ? Map.of() : staticPlaceholders);
        responseBindings = List.copyOf(responseBindings == null ? List.of() : responseBindings);
    }

    /**
     * Returns default mapping profile.
     *
     * @return default mapping.
     */
    public static DialogOpenOptionsMapping defaults() {
        return new DialogOpenOptionsMapping(true, true, Map.of(), List.of());
    }
}
