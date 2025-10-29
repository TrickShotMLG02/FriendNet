package com.trickshotmlg.friendnet.core.permissions;

import com.trickshotmlg.friendnet.core_api.interfaces.PermissionNode;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Permission implements PermissionNode {

    private static String GLOBAL_PREFIX = "friendnet";

    private final String permission;
    private final PermissionNode parent;
    private final List<PermissionNode> children = new ArrayList<>();

    public Permission(String node, PermissionNode parent, boolean inheritParentPermissions) {
        if (inheritParentPermissions) {
            this.permission = (parent != null) ? parent.getPermission() + "." + node : node;
        } else {
            this.permission = node;
        }
        this.parent = parent;
        if (parent != null) parent.addChild(this);
    }

    public Permission(String node, PermissionNode parent) {
        this(node, parent, true);
    }

    @Override
    public void setGlobalPrefix(String prefix) {
        GLOBAL_PREFIX = prefix;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getPermissionPrefixed() {
        if (GLOBAL_PREFIX == null || GLOBAL_PREFIX.isBlank()) return permission;
        return GLOBAL_PREFIX + "." + permission;
    }

    @Override
    public PermissionNode getParent() {
        return parent;
    }

    @Override
    public List<PermissionNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(PermissionNode child) {
        children.add(child);
    }

    @Override
    public boolean has(PlatformPlayer holder) {
        if (holder.hasPermission(getPermissionPrefixed())) return true;
        return parent != null && parent.has(holder);
    }
}
