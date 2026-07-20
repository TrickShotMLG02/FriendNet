package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.UUID;

public record ProxyActionRequestPayload(
        ProxyActionType actionType,
        UUID targetId,
        String targetName,
        boolean refreshFriendList,
        boolean enabled
) {
    public ProxyActionRequestPayload {
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
        targetName = targetName == null ? "" : targetName;
    }

    public ProxyActionRequestPayload(
            ProxyActionType actionType,
            UUID targetId,
            String targetName,
            boolean refreshFriendList
    ) {
        this(actionType, targetId, targetName, refreshFriendList, false);
    }
}
