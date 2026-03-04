package dev.patric.commonlib.api.gui;

import java.util.Objects;

/**
 * Command execution action.
 *
 * @param commandTemplate command template.
 * @param asConsole whether command is executed as console.
 */
public record RunCommandAction(String commandTemplate, boolean asConsole) implements GuiAction {

    /**
     * Compact constructor validation.
     */
    public RunCommandAction {
        commandTemplate = Objects.requireNonNull(commandTemplate, "commandTemplate").trim();
        if (commandTemplate.isEmpty()) {
            throw new IllegalArgumentException("commandTemplate must not be blank");
        }
    }
}
