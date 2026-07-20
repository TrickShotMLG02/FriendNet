package com.trickshotmlg.friendnet.core.application.command;

import java.util.List;
import java.util.UUID;

public record CommandExecutionContext(
        UUID senderId,
        String senderName,
        boolean player,
        CommandPath path,
        List<String> args
) {

    public CommandExecutionContext {
        if (path == null) {
            throw new IllegalArgumentException("Command path cannot be null.");
        }
        args = args == null ? List.of() : List.copyOf(args);
    }
}
