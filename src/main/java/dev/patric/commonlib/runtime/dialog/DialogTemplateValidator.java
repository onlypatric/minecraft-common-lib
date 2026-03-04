package dev.patric.commonlib.runtime.dialog;

import dev.patric.commonlib.api.dialog.DialogInputSpec;
import dev.patric.commonlib.api.dialog.DialogListTypeSpec;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import dev.patric.commonlib.api.dialog.SingleOptionInputSpec;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Validates dialog templates and template reference graph.
 */
public final class DialogTemplateValidator {

    /**
     * Validates a root template with registry-based references.
     *
     * @param root root template.
     * @param registry template registry.
     */
    public void validate(DialogTemplate root, DialogTemplateRegistry registry) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(registry, "registry");

        validateTemplateShape(root);
        validateReferences(root, key -> key.equals(root.templateKey()) ? Optional.of(root) : registry.find(key),
                new LinkedHashSet<>(), new HashSet<>());
    }

    /**
     * Validates basic shape constraints for one template.
     *
     * @param template template.
     */
    public void validateTemplateShape(DialogTemplate template) {
        Objects.requireNonNull(template, "template");

        List<String> inputKeys = template.base().inputs().stream().map(DialogInputSpec::key).toList();
        Set<String> unique = new LinkedHashSet<>(inputKeys);
        if (unique.size() != inputKeys.size()) {
            throw new IllegalArgumentException("duplicate dialog input keys for template " + template.templateKey());
        }

        for (DialogInputSpec inputSpec : template.base().inputs()) {
            if (inputSpec instanceof SingleOptionInputSpec single) {
                long initialCount = single.entries().stream().filter(entry -> entry.initial()).count();
                if (initialCount > 1) {
                    throw new IllegalArgumentException(
                            "single-option input must have at most one initial selection: " + single.key()
                    );
                }
            }
        }
    }

    private void validateReferences(
            DialogTemplate template,
            TemplateLookup lookup,
            Set<String> visiting,
            Set<String> visited
    ) {
        if (visited.contains(template.templateKey())) {
            return;
        }

        if (!visiting.add(template.templateKey())) {
            throw new IllegalArgumentException("dialog template cycle detected: " + template.templateKey());
        }

        if (template.type() instanceof DialogListTypeSpec listSpec) {
            for (String key : listSpec.dialogTemplateKeys()) {
                DialogTemplate referenced = lookup.find(key)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "missing referenced dialog template: " + key + " (from " + template.templateKey() + ")"
                        ));
                validateTemplateShape(referenced);
                validateReferences(referenced, lookup, visiting, visited);
            }
        }

        visiting.remove(template.templateKey());
        visited.add(template.templateKey());
    }

    @FunctionalInterface
    private interface TemplateLookup {
        Optional<DialogTemplate> find(String key);
    }
}
