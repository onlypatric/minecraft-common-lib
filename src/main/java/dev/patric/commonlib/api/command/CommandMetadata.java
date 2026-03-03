package dev.patric.commonlib.api.command;

/**
 * Metadata for command documentation and usage.
 *
 * @param description short description.
 * @param usage usage example.
 */
public record CommandMetadata(String description, String usage) {
}
