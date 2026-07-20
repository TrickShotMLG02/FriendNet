package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.UUID;

public record ProxyActionRequestPayload(
        ProxyActionType actionType,
        UUID targetId,
        String targetName,
        boolean refreshFriendList
) {
    public ProxyActionRequestPayload {
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
        targetName = targetName == null ? "" : targetName;
    }
}
