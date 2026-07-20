package com.trickshotmlg.friendnet.core.application.command;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandRegistryTest {

    @Test
    public void commandPathNormalizesSegments() {
        CommandPath path = CommandPath.of("Friend", "ADD");

        assertEquals("friend add", path.toString());
        assertEquals("add", path.commandName());
        assertTrue(path.startsWith(CommandPath.of("friend")));
    }

    @Test
    public void registryExecutesPlatformOverrideBeforeDefaultHandler() {
        CommandRegistry registry = new CommandRegistry();
        CommandPath path = CommandPath.of("friend", "add");
        registry.register(
                CommandDefinition.builder(path).usage("/friend add <player>").build(),
                context -> CommandUseCaseResult.builder(true)
                        .message(CommandMessage.sender("default"))
                        .build()
        );
        registry.override(path, (context, next) -> CommandUseCaseResult.builder(true)
                .message(CommandMessage.sender(context.args().isEmpty() ? "override" : next.execute(context).messages().get(0).key()))
                .build());

        CommandUseCaseResult overridden = registry.execute(new CommandExecutionContext(null, "Console", false, path, List.of()));
        CommandUseCaseResult delegated = registry.execute(new CommandExecutionContext(null, "Console", false, path, List.of("Alex")));

        assertEquals("override", overridden.messages().get(0).key());
        assertEquals("default", delegated.messages().get(0).key());
    }

    @Test
    public void friendDefinitionsContainExpectedPlatformSpecificCommands() {
        assertTrue(FriendCommandDefinitions.LIST.platformSpecific());
        assertTrue(FriendCommandDefinitions.REQUESTS.platformSpecific());
        assertTrue(FriendCommandDefinitions.RELOAD.platformSpecific());
        assertEquals("/friend add <player>", FriendCommandDefinitions.ADD.usage());
    }
}
