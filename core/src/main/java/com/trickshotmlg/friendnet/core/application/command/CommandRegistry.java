package com.trickshotmlg.friendnet.core.application.command;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {

    private final Map<CommandPath, RegisteredCommand> commands = new LinkedHashMap<>();
    private final Map<CommandPath, CommandOverrideHandler> overrides = new LinkedHashMap<>();

    public void register(CommandDefinition definition, CommandHandler handler) {
        if (definition == null) {
            throw new IllegalArgumentException("Command definition cannot be null.");
        }
        commands.put(definition.path(), new RegisteredCommand(definition, handler));
    }

    public void override(CommandPath path, CommandOverrideHandler overrideHandler) {
        if (path == null) {
            throw new IllegalArgumentException("Command path cannot be null.");
        }
        if (overrideHandler == null) {
            overrides.remove(path);
            return;
        }
        overrides.put(path, overrideHandler);
    }

    public Optional<CommandDefinition> definition(CommandPath path) {
        return Optional.ofNullable(commands.get(path)).map(RegisteredCommand::definition);
    }

    public Collection<CommandDefinition> definitions() {
        return commands.values().stream()
                .map(RegisteredCommand::definition)
                .toList();
    }

    public CommandUseCaseResult execute(CommandExecutionContext context) {
        RegisteredCommand registeredCommand = commands.get(context.path());
        if (registeredCommand == null) {
            return CommandFeedbackUseCases.usage(context.path().toString());
        }

        CommandHandler defaultHandler = registeredCommand.handler();
        if (defaultHandler == null) {
            defaultHandler = ignored -> CommandFeedbackUseCases.usage(registeredCommand.definition().usage());
        }

        CommandOverrideHandler overrideHandler = overrides.get(context.path());
        if (overrideHandler != null) {
            return overrideHandler.execute(context, defaultHandler);
        }

        return defaultHandler.execute(context);
    }

    private record RegisteredCommand(CommandDefinition definition, CommandHandler handler) {
    }
}
