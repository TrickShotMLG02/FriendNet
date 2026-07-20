package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.UUID;

public record ProxyFriendEntry(
        UUID playerId,
        String displayName,
        boolean online,
        String currentServerName
) {
    public ProxyFriendEntry {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
        displayName = displayName == null || displayName.isBlank() ? playerId.toString() : displayName;
        currentServerName = currentServerName == null ? "" : currentServerName;
    }
}
