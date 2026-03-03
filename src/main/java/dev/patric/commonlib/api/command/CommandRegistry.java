package dev.patric.commonlib.api.command;

import java.util.List;
import java.util.Optional;

/**
 * Registry for backend-agnostic command models.
 */
public interface CommandRegistry {

    /**
     * Registers a command model by root.
     *
     * @param model command model.
     */
    void register(CommandModel model);

    /**
     * Finds a command model by root.
     *
     * @param root root command.
     * @return optional command model.
     */
    Optional<CommandModel> find(String root);

    /**
     * Returns all registered command models.
     *
     * @return immutable list.
     */
    List<CommandModel> all();
}
