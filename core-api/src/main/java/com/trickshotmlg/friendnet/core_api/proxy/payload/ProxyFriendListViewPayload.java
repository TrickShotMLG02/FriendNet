package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.List;

public record ProxyFriendListViewPayload(
        List<ProxyFriendEntry> friends,
        List<ProxyFriendEntry> pendingRequests
) {
    public ProxyFriendListViewPayload {
        friends = friends == null ? List.of() : List.copyOf(friends);
        pendingRequests = pendingRequests == null ? List.of() : List.copyOf(pendingRequests);
    }
}
