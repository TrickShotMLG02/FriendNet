package com.trickshotmlg.friendnet.core.application.command;

import java.util.Map;
import java.util.UUID;

public record CommandEvent(
        CommandEventType type,
        UUID actorId,
        UUID targetId,
        Map<String, Object> placeholders
) {
    public CommandEvent {
        placeholders = placeholders == null ? Map.of() : Map.copyOf(placeholders);
    }
}
