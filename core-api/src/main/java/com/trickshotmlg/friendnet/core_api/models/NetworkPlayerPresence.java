package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public record NetworkPlayerPresence(
        UUID playerId,
        String playerName,
        String displayName,
        String serverName,
        boolean online,
        boolean visibleOnline,
        Timestamp lastSeen
) {

    public NetworkPlayerPresence {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }

    public Optional<String> serverNameOptional() {
        return Optional.ofNullable(serverName)
                .filter(value -> !value.isBlank());
    }

    public NetworkPlayerPresence withServerName(String serverName) {
        return new NetworkPlayerPresence(playerId, playerName, displayName, serverName, online, visibleOnline, lastSeen);
    }

    public NetworkPlayerPresence withOnline(boolean online) {
        return new NetworkPlayerPresence(playerId, playerName, displayName, serverName, online, visibleOnline, lastSeen);
    }

    public NetworkPlayerPresence withVisibleOnline(boolean visibleOnline) {
        return new NetworkPlayerPresence(playerId, playerName, displayName, serverName, online, visibleOnline, lastSeen);
    }

    public NetworkPlayerPresence withLastSeen(Timestamp lastSeen) {
        return new NetworkPlayerPresence(playerId, playerName, displayName, serverName, online, visibleOnline, lastSeen);
    }
}
