package com.trickshotmlg.friendnet.core_api.proxy.payload;

import java.util.Map;
import java.util.UUID;

public record ProxyMessagePayload(
        ProxyMessageRecipient recipient,
        UUID recipientId,
        String key,
        Map<String, String> placeholders
) {
    public ProxyMessagePayload {
        if (recipient == null) {
            throw new IllegalArgumentException("recipient cannot be null");
        }
        key = key == null ? "" : key;
        placeholders = placeholders == null ? Map.of() : Map.copyOf(placeholders);
    }
}
