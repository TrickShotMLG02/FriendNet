package com.trickshotmlg.friendnet.core_api.proxy.payload;

public record ProxyDisplayNameUpdatePayload(String displayName, String playerName) {
    public ProxyDisplayNameUpdatePayload {
        displayName = displayName == null ? "" : displayName;
        playerName = playerName == null ? "" : playerName;
    }
}
