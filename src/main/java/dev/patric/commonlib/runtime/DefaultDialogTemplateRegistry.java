package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory template registry.
 */
public final class DefaultDialogTemplateRegistry implements DialogTemplateRegistry {

    private final Map<String, DialogTemplate> templates;

    /**
     * Creates an empty dialog template registry.
     */
    public DefaultDialogTemplateRegistry() {
        this.templates = new ConcurrentHashMap<>();
    }

    @Override
    public void register(DialogTemplate template) {
        DialogTemplate value = Objects.requireNonNull(template, "template");
        DialogTemplate previous = templates.putIfAbsent(value.templateKey(), value);
        if (previous != null) {
            throw new IllegalStateException("Dialog template already registered: " + value.templateKey());
        }
    }

    @Override
    public Optional<DialogTemplate> find(String templateKey) {
        Objects.requireNonNull(templateKey, "templateKey");
        return Optional.ofNullable(templates.get(templateKey));
    }

    @Override
    public List<DialogTemplate> all() {
        return List.copyOf(new ArrayList<>(templates.values()));
    }

    @Override
    public boolean unregister(String templateKey) {
        Objects.requireNonNull(templateKey, "templateKey");
        return templates.remove(templateKey) != null;
    }
}
