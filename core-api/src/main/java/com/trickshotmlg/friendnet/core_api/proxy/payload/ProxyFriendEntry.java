package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.UUID;

public record ProxyFriendEntry(
        UUID playerId,
        String displayName,
        String skinTexture,
        String skinSignature,
        boolean online,
        String currentServerName,
        boolean favourite,
        long requestSentTimeMillis,
        long friendSinceMillis,
        long blockedAtMillis,
        long lastSeenMillis
) {
    public ProxyFriendEntry {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
        displayName = displayName == null || displayName.isBlank() ? playerId.toString() : displayName;
        skinTexture = skinTexture == null ? "" : skinTexture;
        skinSignature = skinSignature == null ? "" : skinSignature;
        currentServerName = currentServerName == null ? "" : currentServerName;
    }

    public ProxyFriendEntry(
            UUID playerId,
            String displayName,
            boolean online,
            String currentServerName,
            boolean favourite,
            long requestSentTimeMillis,
            long friendSinceMillis,
            long blockedAtMillis,
            long lastSeenMillis
    ) {
        this(
                playerId,
                displayName,
                "",
                "",
                online,
                currentServerName,
                favourite,
                requestSentTimeMillis,
                friendSinceMillis,
                blockedAtMillis,
                lastSeenMillis
        );
    }
}
