package dev.patric.commonlib.command;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.command.ArgumentType;
import dev.patric.commonlib.api.command.CommandContext;
import dev.patric.commonlib.api.command.CommandExecution;
import dev.patric.commonlib.api.command.CommandMetadata;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandNode;
import dev.patric.commonlib.api.command.CommandPermission;
import dev.patric.commonlib.api.command.ExecutionMode;
import dev.patric.commonlib.api.command.PermissionPolicy;
import dev.patric.commonlib.api.command.ValidationIssue;
import dev.patric.commonlib.services.DefaultServiceRegistry;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandValidationTest {

    @Test
    void validatorDetectsMissingAndTypeMismatchAndPermission() {
        DefaultCommandValidator validator = new DefaultCommandValidator();
        CommandModel model = new TestModel(
                "duel",
                List.of(
                        new CommandNode("target", ArgumentType.UUID, true, List.of()),
                        new CommandNode("rounds", ArgumentType.INTEGER, true, List.of())
                ),
                new TestExecution(),
                new CommandPermission("commonlib.duel", PermissionPolicy.REQUIRE),
                new CommandMetadata("duel command", "/duel <target> <rounds>")
        );

        CommandContext invalidContext = new TestContext(Map.of(
                "target", "not-uuid",
                "_permission:commonlib.duel", false
        ));

        List<ValidationIssue> issues = validator.validate(invalidContext, model);
        assertEquals(3, issues.size());
        assertTrue(issues.stream().anyMatch(i -> i.code().equals("TYPE_MISMATCH")));
        assertTrue(issues.stream().anyMatch(i -> i.code().equals("REQUIRED")));
        assertTrue(issues.stream().anyMatch(i -> i.code().equals("DENIED")));
    }

    private record TestExecution() implements CommandExecution {
        @Override
        public ExecutionMode mode() {
            return ExecutionMode.SYNC;
        }

        @Override
        public java.util.concurrent.CompletionStage<dev.patric.commonlib.api.command.CommandResult> run(CommandContext context) {
            return CompletableFuture.completedFuture(dev.patric.commonlib.api.command.CommandResult.success());
        }
    }

    private record TestModel(
            String root,
            List<CommandNode> nodes,
            CommandExecution execution,
            CommandPermission permission,
            CommandMetadata metadata
    ) implements CommandModel {
    }

    private static final class TestContext implements CommandContext {

        private final Map<String, Object> args;

        private TestContext(Map<String, Object> args) {
            this.args = args;
        }

        @Override
        public UUID senderId() {
            return UUID.randomUUID();
        }

        @Override
        public Locale locale() {
            return Locale.ENGLISH;
        }

        @Override
        public Map<String, Object> args() {
            return args;
        }

        @Override
        public ServiceRegistry services() {
            return new DefaultServiceRegistry();
        }

        @Override
        public CommonScheduler scheduler() {
            return new CommonScheduler() {
                @Override public dev.patric.commonlib.api.TaskHandle runSync(Runnable task) { task.run(); return new Noop(); }
                @Override public dev.patric.commonlib.api.TaskHandle runSyncLater(long delayTicks, Runnable task) { task.run(); return new Noop(); }
                @Override public dev.patric.commonlib.api.TaskHandle runSyncRepeating(long delayTicks, long periodTicks, Runnable task) { task.run(); return new Noop(); }
                @Override public dev.patric.commonlib.api.TaskHandle runAsync(Runnable task) { task.run(); return new Noop(); }
                @Override public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) { return CompletableFuture.completedFuture(supplier.get()); }
                @Override public boolean isPrimaryThread() { return true; }
                @Override public void requirePrimaryThread(String operationName) { }
            };
        }

        private static final class Noop implements dev.patric.commonlib.api.TaskHandle {
            @Override public void cancel() { }
            @Override public boolean isCancelled() { return false; }
        }
    }
}
