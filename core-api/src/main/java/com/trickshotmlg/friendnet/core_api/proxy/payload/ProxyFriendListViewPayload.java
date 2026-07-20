package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.List;

public record ProxyFriendListViewPayload(
        List<ProxyFriendEntry> friends,
        List<ProxyFriendEntry> pendingRequests,
        List<ProxyFriendEntry> sentRequests,
        List<ProxyFriendEntry> blockedPlayers
) {
    public ProxyFriendListViewPayload {
        friends = friends == null ? List.of() : List.copyOf(friends);
        pendingRequests = pendingRequests == null ? List.of() : List.copyOf(pendingRequests);
        sentRequests = sentRequests == null ? List.of() : List.copyOf(sentRequests);
        blockedPlayers = blockedPlayers == null ? List.of() : List.copyOf(blockedPlayers);
    }
}
