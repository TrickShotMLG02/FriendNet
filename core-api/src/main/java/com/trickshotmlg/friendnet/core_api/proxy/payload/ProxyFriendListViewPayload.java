package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.List;
import java.util.UUID;

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
        long viewerFirstSeenMillis,
        UUID viewedPlayerId,
        String viewedDisplayName,
        long viewedFirstSeenMillis,
        boolean viewedFriendListPublic
) {
    public ProxyFriendListViewPayload {
        friends = friends == null ? List.of() : List.copyOf(friends);
        pendingRequests = pendingRequests == null ? List.of() : List.copyOf(pendingRequests);
        sentRequests = sentRequests == null ? List.of() : List.copyOf(sentRequests);
        blockedPlayers = blockedPlayers == null ? List.of() : List.copyOf(blockedPlayers);
        localeCode = localeCode == null || localeCode.isBlank() ? "en_US" : localeCode;
        viewedDisplayName = viewedDisplayName == null ? "" : viewedDisplayName;
    }

    public ProxyFriendListViewPayload(
            List<ProxyFriendEntry> friends,
            List<ProxyFriendEntry> pendingRequests,
            List<ProxyFriendEntry> sentRequests,
            List<ProxyFriendEntry> blockedPlayers
    ) {
        this(friends, pendingRequests, sentRequests, blockedPlayers, true, true, false, true, true, "en_US", -1L, null, "", -1L, true);
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
                -1L,
                null,
                "",
                -1L,
                true
        );
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
            String localeCode,
            long viewerFirstSeenMillis
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
                viewerFirstSeenMillis,
                null,
                "",
                -1L,
                true
        );
    }
}
