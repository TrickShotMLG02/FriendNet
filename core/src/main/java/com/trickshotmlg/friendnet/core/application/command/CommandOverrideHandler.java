package com.trickshotmlg.friendnet.core.application.command;

@FunctionalInterface
public interface CommandOverrideHandler {
    CommandUseCaseResult execute(CommandExecutionContext context, CommandHandler next);
}
