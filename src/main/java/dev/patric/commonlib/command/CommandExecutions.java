package dev.patric.commonlib.command;

import dev.patric.commonlib.api.command.CommandContext;
import dev.patric.commonlib.api.command.CommandExecution;
import dev.patric.commonlib.api.command.CommandResult;
import dev.patric.commonlib.api.command.ExecutionMode;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Utility for running command execution with sync handoff guarantees.
 */
public final class CommandExecutions {

    private CommandExecutions() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Executes a command with mode-aware scheduling behavior.
     *
     * @param context command context.
     * @param execution execution contract.
     * @return completion future.
     */
    public static CompletableFuture<CommandResult> execute(CommandContext context, CommandExecution execution) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(execution, "execution");

        CompletableFuture<CommandResult> stage = execution.run(context)
                .toCompletableFuture()
                .exceptionally(ex -> CommandResult.failure("EXECUTION_ERROR", "command.execution.error"));

        if (execution.mode() != ExecutionMode.ASYNC_IO_THEN_SYNC) {
            return stage;
        }

        return stage.thenCompose(result -> {
            if (context.scheduler().isPrimaryThread()) {
                return CompletableFuture.completedFuture(result);
            }
            CompletableFuture<CommandResult> synced = new CompletableFuture<>();
            context.scheduler().runSync(() -> synced.complete(result));
            return synced;
        }).exceptionally(ex -> {
            Throwable cause = ex instanceof CompletionException ce ? ce.getCause() : ex;
            return CommandResult.failure("EXECUTION_PIPELINE_ERROR", "command.execution.pipeline_error:" + cause.getClass().getSimpleName());
        });
    }
}
