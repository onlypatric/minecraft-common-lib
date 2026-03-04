package dev.patric.commonlib.api.dialog;

/**
 * Marker for dialog action specifications.
 */
public sealed interface DialogActionSpec permits CommandTemplateActionSpec, StaticClickActionSpec, CustomActionSpec {
}
