package com.trickshotmlg.friendnet.core_api.proxy.payload;

public record ProxyDisplayNameUpdatePayload(String displayName) {
    public ProxyDisplayNameUpdatePayload {
        displayName = displayName == null ? "" : displayName;
    }
}
