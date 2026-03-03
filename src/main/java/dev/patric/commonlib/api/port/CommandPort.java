package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.command.CommandModel;

/**
 * Command integration port.
 */
public interface CommandPort {

    /**
     * Registers a command model in the backend.
     *
     * @param model command model.
     */
    void register(CommandModel model);

    /**
     * Unregisters command by root label.
     *
     * @param root root command.
     */
    void unregister(String root);

    /**
     * Indicates whether backend supports command suggestions.
     *
     * @return true when suggestions are supported.
     */
    boolean supportsSuggestions();
}
