package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.List;

public record ProxyActionResponsePayload(
        boolean success,
        List<ProxyMessagePayload> messages,
        ProxyFriendListViewPayload friendListView
) {
    public ProxyActionResponsePayload {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }
}
