package dev.patric.commonlib.api.dialog;

/**
 * Marker for dialog type specifications.
 */
public sealed interface DialogTypeSpec permits ConfirmationTypeSpec, NoticeTypeSpec, MultiActionTypeSpec, DialogListTypeSpec, ServerLinksTypeSpec {
}
