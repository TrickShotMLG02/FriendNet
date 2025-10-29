package com.trickshotmlg.friendnet.core.permissions;

public class PermissionHolder {

    ///////////////////////////////////////////
    ///         ADMIN PERMISSIONS           ///
    ///////////////////////////////////////////
    public static final Permission FRIENDS_ADMIN = new Permission("admin", null);

    public static final Permission FRIENDS_RELOAD = new Permission("reload", FRIENDS_ADMIN);


    ///////////////////////////////////////////
    ///         BASIC PERMISSIONS           ///
    ///////////////////////////////////////////
    public static final Permission FRIENDS_BASIC = new Permission("basic", FRIENDS_ADMIN);

    // Friends
    public static final Permission FRIEND_LIST = new Permission("friends.list", FRIENDS_BASIC);
    public static final Permission FRIEND_ADD = new Permission("friends.add", FRIENDS_BASIC);
    public static final Permission FRIEND_REMOVE = new Permission("friends.remove", FRIENDS_BASIC);

    // Requests
    public static final Permission FRIEND_REQUESTS = new Permission("friends.requests", FRIENDS_BASIC, false);

    public static final Permission FRIEND_REQUESTS_LIST = new Permission("list", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_ACCEPT = new Permission("accept", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_CANCEL = new Permission("cancel", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_DENY = new Permission("deny", FRIEND_REQUESTS);

    // TODO: Implement more permissions
}
