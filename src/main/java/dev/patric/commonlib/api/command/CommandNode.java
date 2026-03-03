package dev.patric.commonlib.api.command;

import java.util.List;

/**
 * Command argument node definition.
 *
 * @param name argument name.
 * @param type argument type.
 * @param required whether argument is mandatory.
 * @param constraints additional constraints.
 */
public record CommandNode(String name, ArgumentType type, boolean required, List<CommandConstraint> constraints) {

    /**
     * Compact constructor normalization.
     */
    public CommandNode {
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
    }
}
