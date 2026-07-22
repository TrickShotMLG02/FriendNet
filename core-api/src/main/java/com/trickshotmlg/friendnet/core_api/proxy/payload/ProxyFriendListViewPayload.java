package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.List;

public record ProxyFriendListViewPayload(
        List<ProxyFriendEntry> friends,
        List<ProxyFriendEntry> pendingRequests,
        List<ProxyFriendEntry> sentRequests,
        List<ProxyFriendEntry> blockedPlayers,
        boolean allowFriendRequests,
        boolean showOnlineStatus,
        boolean autoAcceptFriends,
        boolean friendRequestNotifications,
        boolean friendListPublic,
        String localeCode,
        long viewerFirstSeenMillis
) {
    public ProxyFriendListViewPayload {
        friends = friends == null ? List.of() : List.copyOf(friends);
        pendingRequests = pendingRequests == null ? List.of() : List.copyOf(pendingRequests);
        sentRequests = sentRequests == null ? List.of() : List.copyOf(sentRequests);
        blockedPlayers = blockedPlayers == null ? List.of() : List.copyOf(blockedPlayers);
        localeCode = localeCode == null || localeCode.isBlank() ? "en_US" : localeCode;
    }

    public ProxyFriendListViewPayload(
            List<ProxyFriendEntry> friends,
            List<ProxyFriendEntry> pendingRequests,
            List<ProxyFriendEntry> sentRequests,
            List<ProxyFriendEntry> blockedPlayers
    ) {
        this(friends, pendingRequests, sentRequests, blockedPlayers, true, true, false, true, true, "en_US", -1L);
    }

    public ProxyFriendListViewPayload(
            List<ProxyFriendEntry> friends,
            List<ProxyFriendEntry> pendingRequests,
            List<ProxyFriendEntry> sentRequests,
            List<ProxyFriendEntry> blockedPlayers,
            boolean allowFriendRequests,
            boolean showOnlineStatus,
            boolean autoAcceptFriends,
            boolean friendRequestNotifications,
            boolean friendListPublic,
            String localeCode
    ) {
        this(
                friends,
                pendingRequests,
                sentRequests,
                blockedPlayers,
                allowFriendRequests,
                showOnlineStatus,
                autoAcceptFriends,
                friendRequestNotifications,
                friendListPublic,
                localeCode,
                -1L
        );
    }
}
