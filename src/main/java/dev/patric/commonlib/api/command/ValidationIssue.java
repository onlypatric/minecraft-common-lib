package dev.patric.commonlib.api.command;

/**
 * Validation issue returned by command validators.
 *
 * @param field field name.
 * @param code machine code.
 * @param messageKey i18n message key.
 */
public record ValidationIssue(String field, String code, String messageKey) {
}
