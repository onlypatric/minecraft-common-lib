package dev.patric.commonlib.api.dialog;

/**
 * Marker for dialog input specifications.
 */
public sealed interface DialogInputSpec permits TextInputSpec, BooleanInputSpec, NumberRangeInputSpec, SingleOptionInputSpec {

    /**
     * Returns unique input key.
     *
     * @return input key.
     */
    String key();
}
