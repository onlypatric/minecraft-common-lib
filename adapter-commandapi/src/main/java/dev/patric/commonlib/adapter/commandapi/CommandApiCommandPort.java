package dev.patric.commonlib.adapter.commandapi;

import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.port.CommandPort;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command port backed by CommandAPI runtime presence.
 */
public final class CommandApiCommandPort implements CommandPort {

    private final Map<String, CommandModel> models = new ConcurrentHashMap<>();

    @Override
    public void register(CommandModel model) {
        Objects.requireNonNull(model, "model");
        String root = Objects.requireNonNull(model.root(), "model.root").trim().toLowerCase();
        if (root.isEmpty()) {
            throw new IllegalArgumentException("model.root must not be blank");
        }

        CommandModel previous = models.putIfAbsent(root, model);
        if (previous != null) {
            throw new IllegalStateException("Command already registered: " + model.root());
        }
    }

    @Override
    public void unregister(String root) {
        String normalized = Objects.requireNonNull(root, "root").trim().toLowerCase();
        if (normalized.isEmpty()) {
            return;
        }
        models.remove(normalized);
    }

    @Override
    public boolean supportsSuggestions() {
        return true;
    }
}
