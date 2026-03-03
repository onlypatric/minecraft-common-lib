package dev.patric.commonlib.api.command;

import java.util.concurrent.CompletionStage;

/**
 * Command execution contract.
 */
public interface CommandExecution {

    /**
     * Returns execution mode.
     *
     * @return mode.
     */
    ExecutionMode mode();

    /**
     * Executes command logic.
     *
     * @param context command context.
     * @return completion stage result.
     */
    CompletionStage<CommandResult> run(CommandContext context);
}
