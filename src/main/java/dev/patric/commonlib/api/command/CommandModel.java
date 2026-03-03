package dev.patric.commonlib.api.command;

import java.util.List;

/**
 * Backend-agnostic command model.
 */
public interface CommandModel {

    /**
     * Root command label.
     *
     * @return root command.
     */
    String root();

    /**
     * Command argument nodes.
     *
     * @return ordered nodes.
     */
    List<CommandNode> nodes();

    /**
     * Execution behavior.
     *
     * @return execution contract.
     */
    CommandExecution execution();

    /**
     * Permission declaration.
     *
     * @return permission declaration.
     */
    CommandPermission permission();

    /**
     * Additional metadata.
     *
     * @return metadata.
     */
    CommandMetadata metadata();
}
