package dev.patric.commonlib.command;

import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.TaskHandle;
import dev.patric.commonlib.api.command.CommandContext;
import dev.patric.commonlib.api.command.CommandExecution;
import dev.patric.commonlib.api.command.CommandResult;
import dev.patric.commonlib.api.command.ExecutionMode;
import dev.patric.commonlib.services.DefaultServiceRegistry;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandSchedulerIntegrationTest {

    @Test
    void asyncExecutionIsHandedOffToSyncCompletionPath() {
        TestScheduler scheduler = new TestScheduler();
        CommandContext context = new SimpleContext(scheduler);

        CommandExecution execution = new CommandExecution() {
            @Override
            public ExecutionMode mode() {
                return ExecutionMode.ASYNC_IO_THEN_SYNC;
            }

            @Override
            public java.util.concurrent.CompletionStage<CommandResult> run(CommandContext ctx) {
                return CompletableFuture.supplyAsync(CommandResult::success);
            }
        };

        CommandResult result = CommandExecutions.execute(context, execution).join();
        assertTrue(result.successful());
        assertTrue(scheduler.syncRan.get());
        assertEquals("SUCCESS", result.code());
    }

    private record SimpleContext(TestScheduler scheduler) implements CommandContext {
        @Override public UUID senderId() { return UUID.randomUUID(); }
        @Override public Locale locale() { return Locale.ENGLISH; }
        @Override public Map<String, Object> args() { return Map.of(); }
        @Override public ServiceRegistry services() { return new DefaultServiceRegistry(); }
    }

    private static final class TestScheduler implements CommonScheduler {
        private final AtomicBoolean syncRan = new AtomicBoolean(false);

        @Override
        public TaskHandle runSync(Runnable task) {
            syncRan.set(true);
            task.run();
            return new NoopHandle();
        }

        @Override public TaskHandle runSyncLater(long delayTicks, Runnable task) { return runSync(task); }
        @Override public TaskHandle runSyncRepeating(long delayTicks, long periodTicks, Runnable task) { return runSync(task); }
        @Override public TaskHandle runAsync(Runnable task) { task.run(); return new NoopHandle(); }
        @Override public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) { return CompletableFuture.completedFuture(supplier.get()); }
        @Override public boolean isPrimaryThread() { return false; }
        @Override public void requirePrimaryThread(String operationName) { throw new IllegalStateException(operationName); }
    }

    private static final class NoopHandle implements TaskHandle {
        @Override public void cancel() { }
        @Override public boolean isCancelled() { return false; }
    }
}
