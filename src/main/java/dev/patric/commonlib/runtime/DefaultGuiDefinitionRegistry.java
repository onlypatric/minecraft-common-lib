package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.gui.GuiDefinition;
import dev.patric.commonlib.api.gui.GuiDefinitionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory implementation of GUI definition registry.
 */
public final class DefaultGuiDefinitionRegistry implements GuiDefinitionRegistry {

    private final Map<String, GuiDefinition> definitions = new ConcurrentHashMap<>();

    @Override
    public void register(GuiDefinition definition) {
        GuiDefinition normalized = Objects.requireNonNull(definition, "definition");
        definitions.put(normalized.key(), normalized);
    }

    @Override
    public Optional<GuiDefinition> find(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(definitions.get(key));
    }

    @Override
    public boolean unregister(String key) {
        Objects.requireNonNull(key, "key");
        return definitions.remove(key) != null;
    }

    @Override
    public List<GuiDefinition> all() {
        return List.copyOf(new ArrayList<>(definitions.values()));
    }
}
