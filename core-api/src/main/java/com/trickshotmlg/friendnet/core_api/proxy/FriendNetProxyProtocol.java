package com.trickshotmlg.friendnet.core_api.proxy;

public final class FriendNetProxyProtocol {

    public static final String CHANNEL = "friendnet:proxy";
    public static final int VERSION = 1;
    public static final String DEFAULT_CONNECTION_TOKEN = "change-me";
    public static final long MAX_CLOCK_SKEW_MILLIS = 60_000L;

    private FriendNetProxyProtocol() {
    }

    public static boolean isUnsafeToken(String token) {
        return token == null
                || token.isBlank()
                || DEFAULT_CONNECTION_TOKEN.equals(token);
    }
}
