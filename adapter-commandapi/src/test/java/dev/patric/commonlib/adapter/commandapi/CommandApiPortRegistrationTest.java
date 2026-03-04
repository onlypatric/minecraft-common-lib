package dev.patric.commonlib.adapter.commandapi;

import dev.patric.commonlib.api.command.CommandExecution;
import dev.patric.commonlib.api.command.CommandMetadata;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandNode;
import dev.patric.commonlib.api.command.CommandPermission;
import dev.patric.commonlib.api.command.CommandResult;
import dev.patric.commonlib.api.command.ExecutionMode;
import dev.patric.commonlib.api.command.PermissionPolicy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandApiPortRegistrationTest {

    @Test
    void registerUnregisterAndDuplicateBehaviorAreDeterministic() {
        CommandApiCommandPort port = new CommandApiCommandPort();

        port.register(testModel("warp"));
        assertTrue(port.supportsSuggestions());

        assertThrows(IllegalStateException.class, () -> port.register(testModel("Warp")));
        assertDoesNotThrow(() -> port.unregister("warp"));
        assertDoesNotThrow(() -> port.unregister("unknown"));
    }

    @Test
    void blankRootIsRejected() {
        CommandApiCommandPort port = new CommandApiCommandPort();
        assertThrows(IllegalArgumentException.class, () -> port.register(testModel("   ")));
    }

    private static CommandModel testModel(String root) {
        return new CommandModel() {
            @Override
            public String root() {
                return root;
            }

            @Override
            public List<CommandNode> nodes() {
                return List.of();
            }

            @Override
            public CommandExecution execution() {
                return new CommandExecution() {
                    @Override
                    public ExecutionMode mode() {
                        return ExecutionMode.SYNC;
                    }

                    @Override
                    public CompletionStage<CommandResult> run(dev.patric.commonlib.api.command.CommandContext context) {
                        return CompletableFuture.completedFuture(CommandResult.success());
                    }
                };
            }

            @Override
            public CommandPermission permission() {
                return new CommandPermission("commonlib.test", PermissionPolicy.OPTIONAL);
            }

            @Override
            public CommandMetadata metadata() {
                return new CommandMetadata("test", "/" + root);
            }
        };
    }
}
