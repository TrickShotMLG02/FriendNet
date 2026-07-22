package com.trickshotmlg.friendnet.core.application.command;

import java.util.Map;

public final class CommandFeedbackUseCases {

    private CommandFeedbackUseCases() {
    }

    public static CommandUseCaseResult playersOnly() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.playersOnlyCommand"))
                .build();
    }

    public static CommandUseCaseResult usage(String usage) {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.usage", Map.of("usage", usage)))
                .build();
    }

    public static CommandUseCaseResult playerNotFound() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.playerNotFound"))
                .build();
    }

    public static CommandUseCaseResult noPermission() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("noPermission"))
                .build();
    }

    public static CommandUseCaseResult friendListPrivate() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("friendList.private"))
                .build();
    }

    public static CommandUseCaseResult reload(boolean success) {
        return CommandUseCaseResult.builder(success)
                .message(CommandMessage.sender(success ? "configReloadSuccess" : "configReloadError"))
                .build();
    }

    public static CommandUseCaseResult proxyBackendCommandDisabled() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.proxyBackendCommandDisabled"))
                .build();
    }

    public static CommandUseCaseResult proxyBackendGuiUnavailable() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.proxyBackendGuiUnavailable"))
                .build();
    }

    public static CommandUseCaseResult proxySyncQueued(int count) {
        return CommandUseCaseResult.builder(true)
                .message(CommandMessage.sender("commandFeedback.proxySyncQueued", Map.of("count", count)))
                .build();
    }

    public static CommandUseCaseResult proxySyncUnavailable() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.proxySyncUnavailable"))
                .build();
    }

    public static CommandUseCaseResult proxyReloadUnavailable() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.proxyReloadUnavailable"))
                .build();
    }

    public static CommandUseCaseResult proxyHandshakeQueued() {
        return CommandUseCaseResult.builder(true)
                .message(CommandMessage.sender("commandFeedback.proxyHandshakeQueued"))
                .build();
    }

    public static CommandUseCaseResult proxyHandshakeUnavailable() {
        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("commandFeedback.proxyHandshakeUnavailable"))
                .build();
    }
}
