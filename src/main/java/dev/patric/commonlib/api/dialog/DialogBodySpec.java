package dev.patric.commonlib.api.dialog;

/**
 * Marker for dialog body specifications.
 */
public sealed interface DialogBodySpec permits PlainMessageBodySpec, ItemBodySpec {
}
