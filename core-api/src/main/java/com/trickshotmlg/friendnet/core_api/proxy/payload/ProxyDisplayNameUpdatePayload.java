package com.trickshotmlg.friendnet.core_api.proxy.payload;

public record ProxyDisplayNameUpdatePayload(String displayName, String playerName, String skinTexture, String skinSignature) {
    public ProxyDisplayNameUpdatePayload {
        displayName = displayName == null ? "" : displayName;
        playerName = playerName == null ? "" : playerName;
        skinTexture = skinTexture == null ? "" : skinTexture;
        skinSignature = skinSignature == null ? "" : skinSignature;
    }

    public ProxyDisplayNameUpdatePayload(String displayName, String playerName) {
        this(displayName, playerName, "", "");
    }
}
