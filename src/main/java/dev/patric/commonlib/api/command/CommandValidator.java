package dev.patric.commonlib.api.command;

import java.util.List;

/**
 * Validates command context against command model rules.
 */
public interface CommandValidator {

    /**
     * Validates context/model pair.
     *
     * @param context command context.
     * @param model command model.
     * @return validation issues.
     */
    List<ValidationIssue> validate(CommandContext context, CommandModel model);
}
