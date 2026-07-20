package com.trickshotmlg.friendnet.core.application.command;

@FunctionalInterface
public interface CommandHandler {
    CommandUseCaseResult execute(CommandExecutionContext context);
}
