package com.trickshotmlg.friendnet.core.permissions;

public class PermissionHolder {

    ///////////////////////////////////////////
    ///         ADMIN PERMISSIONS           ///
    ///////////////////////////////////////////
    public static final Permission FRIENDS_ADMIN = new Permission("admin", null);

    public static final Permission FRIENDS_RELOAD = new Permission("reload", FRIENDS_ADMIN, false);
    public static final Permission FRIENDS_PROXY_HANDSHAKE = new Permission("proxyhandshake", FRIENDS_ADMIN, false);
    public static final Permission FRIENDS_PROXY_SYNC = new Permission("proxysync", FRIENDS_ADMIN, false);


    ///////////////////////////////////////////
    ///         BASIC PERMISSIONS           ///
    ///////////////////////////////////////////
    public static final Permission FRIENDS_BASIC = new Permission("basic", FRIENDS_ADMIN, false);

    // Friends
    public static final Permission FRIENDS = new Permission("friends", FRIENDS_BASIC, false);

    public static final Permission FRIEND_LIST = new Permission("list", FRIENDS);
    public static final Permission FRIEND_ADD = new Permission("add", FRIENDS);
    public static final Permission FRIEND_REMOVE = new Permission("remove", FRIENDS);
    public static final Permission FRIEND_BLOCK = new Permission("block", FRIENDS);
    public static final Permission FRIEND_UNBLOCK = new Permission("unblock", FRIENDS);

    // Requests
    public static final Permission FRIEND_REQUESTS = new Permission("friends.requests", FRIENDS_BASIC, false);

    public static final Permission FRIEND_REQUESTS_LIST = new Permission("list", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_ACCEPT = new Permission("accept", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_ACCEPT_ALL = new Permission("acceptall", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_CANCEL = new Permission("cancel", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_DENY = new Permission("deny", FRIEND_REQUESTS);
    public static final Permission FRIEND_REQUESTS_DENY_ALL = new Permission("denyall", FRIEND_REQUESTS);

    // TODO: Implement more permissions
}
