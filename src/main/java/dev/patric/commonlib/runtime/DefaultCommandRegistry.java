package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandRegistry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe default command registry.
 */
public final class DefaultCommandRegistry implements CommandRegistry {

    private final Map<String, CommandModel> models = new ConcurrentHashMap<>();

    @Override
    public void register(CommandModel model) {
        Objects.requireNonNull(model, "model");
        String key = Objects.requireNonNull(model.root(), "model.root").toLowerCase();
        CommandModel previous = models.putIfAbsent(key, model);
        if (previous != null) {
            throw new IllegalStateException("Command already registered: " + model.root());
        }
    }

    @Override
    public Optional<CommandModel> find(String root) {
        Objects.requireNonNull(root, "root");
        return Optional.ofNullable(models.get(root.toLowerCase()));
    }

    @Override
    public List<CommandModel> all() {
        return List.copyOf(models.values());
    }
}
