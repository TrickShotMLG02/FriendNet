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
}
