package com.trickshotmlg.friendnet.core.application.command;

import java.util.Map;
import java.util.UUID;

public record CommandMessage(
        CommandMessageRecipient recipient,
        UUID recipientId,
        String key,
        Map<String, Object> placeholders
) {
    public CommandMessage {
        placeholders = placeholders == null ? Map.of() : Map.copyOf(placeholders);
    }

    public static CommandMessage sender(String key) {
        return sender(key, Map.of());
    }

    public static CommandMessage sender(String key, Map<String, Object> placeholders) {
        return new CommandMessage(CommandMessageRecipient.SENDER, null, key, placeholders);
    }

    public static CommandMessage player(UUID playerId, String key, Map<String, Object> placeholders) {
        return new CommandMessage(CommandMessageRecipient.PLAYER, playerId, key, placeholders);
    }
}
