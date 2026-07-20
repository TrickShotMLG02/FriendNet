package com.trickshotmlg.friendnet.core_api.interfaces;

import java.util.*;

public interface PermissionNode {

    /**
     * Sets the prefix for all permissions
     *
     * @param prefix the prefix to use for all permissions
     */
    void setGlobalPrefix(String prefix);

    /**
     * Gets the actual permission as string
     *
     * @return The permission string
     */
    String getPermission();

    /**
     * Gets the actual permission as string with a prefix
     *
     * @return The full permission string with a prefix
     */
    String getPermissionPrefixed();

    /**
     * Gets the parent permission node
     *
     * @return The parent of this permission node, {@code null} if root
     */
    PermissionNode getParent();

    /**
     * Gets the list of all child permission nodes
     *
     * @return A list containing all direct child permission nodes
     */
    List<PermissionNode> getChildren();

    /**
     * Adds a child node to this permission node.
     */
    void addChild(PermissionNode child);

    /**
     * Checks if the provided player abstraction has this permission.
     * The actual permission check is implemented per platform.
     *
     * @return {@code true} if the PlatformPlayer has the permission, {@code false} otherwise
     */
    boolean has(PlatformPlayer holder);

    /**
     * Returns true if this permission node or any of its parents match the given permission string.
     */
    default boolean matches(String permission) {
        if (getPermission().equalsIgnoreCase(permission)) return true;
        PermissionNode parent = getParent();
        return parent != null && parent.matches(permission);
    }

    /**
     * Checks this node and every parent node using the supplied platform permission check.
     */
    default boolean anyParentGranted(java.util.function.Predicate<String> permissionCheck) {
        if (permissionCheck.test(getPermissionPrefixed())) {
            return true;
        }

        PermissionNode parent = getParent();
        return parent != null && parent.anyParentGranted(permissionCheck);
    }
}
