package dev.patric.commonlib.api.command;

import java.util.List;

/**
 * Command execution result.
 *
 * @param successful whether execution succeeded.
 * @param code machine code.
 * @param messageKey i18n message key.
 * @param validationIssues validation issues.
 */
public record CommandResult(boolean successful, String code, String messageKey, List<ValidationIssue> validationIssues) {

    /**
     * Success result.
     *
     * @return success result.
     */
    public static CommandResult success() {
        return new CommandResult(true, "SUCCESS", "command.success", List.of());
    }

    /**
     * Generic failure.
     *
     * @param code machine code.
     * @param messageKey message key.
     * @return failure result.
     */
    public static CommandResult failure(String code, String messageKey) {
        return new CommandResult(false, code, messageKey, List.of());
    }

    /**
     * Validation failure helper.
     *
     * @param field field name.
     * @param reason reason/code.
     * @return failure result with one validation issue.
     */
    public static CommandResult validationFailure(String field, String reason) {
        ValidationIssue issue = new ValidationIssue(field, reason, "command.validation." + reason);
        return new CommandResult(false, "VALIDATION_ERROR", issue.messageKey(), List.of(issue));
    }
}
