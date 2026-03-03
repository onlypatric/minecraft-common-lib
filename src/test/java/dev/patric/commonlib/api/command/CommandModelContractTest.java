package dev.patric.commonlib.api.command;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandModelContractTest {

    @Test
    void commandModelContractsExposeExpectedSignatures() throws Exception {
        assertMethod(CommandModel.class, "root", String.class);
        assertMethod(CommandModel.class, "nodes", List.class);
        assertMethod(CommandModel.class, "execution", CommandExecution.class);
        assertMethod(CommandModel.class, "permission", CommandPermission.class);
        assertMethod(CommandModel.class, "metadata", CommandMetadata.class);

        assertMethod(CommandExecution.class, "mode", ExecutionMode.class);
        assertMethod(CommandExecution.class, "run", CompletionStage.class, CommandContext.class);

        assertMethod(CommandContext.class, "senderId", UUID.class);
        assertMethod(CommandContext.class, "locale", java.util.Locale.class);
        assertMethod(CommandContext.class, "args", Map.class);
        assertMethod(CommandContext.class, "services", dev.patric.commonlib.api.ServiceRegistry.class);
        assertMethod(CommandContext.class, "scheduler", dev.patric.commonlib.api.CommonScheduler.class);

        assertMethod(CommandValidator.class, "validate", List.class, CommandContext.class, CommandModel.class);
        assertMethod(CommandRegistry.class, "register", void.class, CommandModel.class);
        assertMethod(CommandRegistry.class, "find", Optional.class, String.class);
        assertMethod(CommandRegistry.class, "all", List.class);
    }

    private static void assertMethod(Class<?> type, String method, Class<?> returnType, Class<?>... params)
            throws NoSuchMethodException {
        Method m = type.getMethod(method, params);
        assertEquals(returnType, m.getReturnType());
    }
}
