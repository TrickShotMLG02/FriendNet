package com.trickshotmlg.friendnet.core_api.enums;

import java.util.Locale;

public enum NetworkRole {
    STANDALONE,
    PROXY_AUTHORITY,
    PROXY_BACKEND;

    public boolean isProxyMode() {
        return this == PROXY_AUTHORITY || this == PROXY_BACKEND;
    }

    public boolean ownsPersistentState() {
        return this == STANDALONE || this == PROXY_AUTHORITY;
    }

    public boolean delegatesPersistentState() {
        return this == PROXY_BACKEND;
    }

    public static NetworkRole parse(String value) {
        if (value == null || value.isBlank()) {
            return STANDALONE;
        }

        String normalized = value.trim()
                .replace("-", "")
                .replace("_", "")
                .toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "standalone" -> STANDALONE;
            case "proxy", "proxyauthority", "authority" -> PROXY_AUTHORITY;
            case "backend", "proxybackend" -> PROXY_BACKEND;
            default -> throw new IllegalArgumentException("Unknown network role: " + value);
        };
    }
}
