package dev.patric.commonlib.api.command;

/**
 * Supported argument types for command parsing/validation.
 */
public enum ArgumentType {
    STRING,
    INTEGER,
    DOUBLE,
    BOOLEAN,
    UUID;

    /**
     * Validates whether a value matches the expected argument type.
     *
     * @param value value.
     * @return true when value matches this type.
     */
    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }

        return switch (this) {
            case STRING -> value instanceof String;
            case INTEGER -> value instanceof Integer;
            case DOUBLE -> value instanceof Double || value instanceof Float;
            case BOOLEAN -> value instanceof Boolean;
            case UUID -> value instanceof java.util.UUID;
        };
    }
}
