package dev.patric.commonlib.command;

import dev.patric.commonlib.api.command.CommandContext;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandNode;
import dev.patric.commonlib.api.command.CommandValidator;
import dev.patric.commonlib.api.command.PermissionPolicy;
import dev.patric.commonlib.api.command.ValidationIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Baseline command validator for args and permission policy.
 */
public final class DefaultCommandValidator implements CommandValidator {

    @Override
    public List<ValidationIssue> validate(CommandContext context, CommandModel model) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(model, "model");

        List<ValidationIssue> issues = new ArrayList<>();
        Map<String, Object> args = context.args();

        for (CommandNode node : model.nodes()) {
            Object value = args.get(node.name());

            if (node.required() && value == null) {
                issues.add(new ValidationIssue(node.name(), "REQUIRED", "command.validation.required"));
                continue;
            }

            if (value != null && !node.type().matches(value)) {
                issues.add(new ValidationIssue(node.name(), "TYPE_MISMATCH", "command.validation.type_mismatch"));
                continue;
            }

            if (value != null) {
                node.constraints().forEach(constraint ->
                        constraint.validate(node.name(), value, context).ifPresent(issues::add)
                );
            }
        }

        PermissionPolicy policy = model.permission().policy();
        if (policy == PermissionPolicy.DISABLE_COMMAND) {
            issues.add(new ValidationIssue("permission", "DISABLED", "command.validation.disabled"));
        } else if (policy == PermissionPolicy.REQUIRE && !hasPermission(context, model.permission().node())) {
            issues.add(new ValidationIssue("permission", "DENIED", "command.validation.permission_denied"));
        }

        return issues;
    }

    private boolean hasPermission(CommandContext context, String node) {
        if (node == null || node.isBlank()) {
            return true;
        }
        Object raw = context.args().get("_permission:" + node);
        return raw instanceof Boolean value && value;
    }
}
