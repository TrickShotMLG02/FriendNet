package com.trickshotmlg.friendnet.core_api.constants;

public class FriendNetPermissions {
    private static final String PERMISSION_PREFIX = "friendnet";

    // Admin Permissions
    public static final String RELOAD = PERMISSION_PREFIX + ".reload";

    // Default Permissions
    public static final String FRIEND_USE = PERMISSION_PREFIX + ".friends.use";
    public static final String FRIEND_LIST = PERMISSION_PREFIX + ".friends.list";

    // Friend Management Permissions
    public static final String FRIEND_ADD = PERMISSION_PREFIX + ".friends.add";
    public static final String FRIEND_ACCEPT = PERMISSION_PREFIX + ".friends.accept";
    public static final String FRIEND_REMOVE = PERMISSION_PREFIX + ".friends.remove";
    public static final String FRIEND_REQUESTS = PERMISSION_PREFIX + ".friends.requests";
}
