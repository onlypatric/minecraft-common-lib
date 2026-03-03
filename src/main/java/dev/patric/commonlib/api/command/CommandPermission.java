package dev.patric.commonlib.api.command;

/**
 * Permission declaration for a command model.
 *
 * @param node permission node.
 * @param policy permission policy.
 */
public record CommandPermission(String node, PermissionPolicy policy) {

    /**
     * Optional permission helper.
     *
     * @return optional policy permission.
     */
    public static CommandPermission optional() {
        return new CommandPermission("", PermissionPolicy.OPTIONAL);
    }
}
