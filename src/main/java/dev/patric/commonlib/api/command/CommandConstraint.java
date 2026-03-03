package dev.patric.commonlib.api.command;

import java.util.Optional;

/**
 * Additional validation constraint for a command argument.
 */
@FunctionalInterface
public interface CommandConstraint {

    /**
     * Validates a command argument value.
     *
     * @param field argument name.
     * @param value argument value.
     * @param context command context.
     * @return optional validation issue.
     */
    Optional<ValidationIssue> validate(String field, Object value, CommandContext context);
}
