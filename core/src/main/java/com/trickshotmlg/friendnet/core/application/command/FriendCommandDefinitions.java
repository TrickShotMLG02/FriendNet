package com.trickshotmlg.friendnet.core.application.command;

import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;

import java.util.List;

public final class FriendCommandDefinitions {

    public static final CommandPath FRIEND = CommandPath.of("friend");
    public static final CommandPath FRIENDS = CommandPath.of("friends");

    public static final CommandDefinition ROOT = CommandDefinition.builder(FRIEND)
            .description("Manage your friends")
            .usage("/friend <subcommand>")
            .permission(PermissionHolder.FRIENDS)
            .aliases(List.of("friends"))
            .build();

    public static final CommandDefinition ADD = CommandDefinition.builder(FRIEND.append("add"))
            .description("Send a friend request to a player")
            .usage("/friend add <player>")
            .permission(PermissionHolder.FRIEND_ADD)
            .playerOnly(true)
            .build();

    public static final CommandDefinition REMOVE = CommandDefinition.builder(FRIEND.append("remove"))
            .description("Remove a friend")
            .usage("/friend remove <player>")
            .permission(PermissionHolder.FRIEND_REMOVE)
            .playerOnly(true)
            .build();

    public static final CommandDefinition BLOCK = CommandDefinition.builder(FRIEND.append("block"))
            .description("Block a player")
            .usage("/friend block <player>")
            .permission(PermissionHolder.FRIEND_BLOCK)
            .playerOnly(true)
            .build();

    public static final CommandDefinition UNBLOCK = CommandDefinition.builder(FRIEND.append("unblock"))
            .description("Unblock a player")
            .usage("/friend unblock <player>")
            .permission(PermissionHolder.FRIEND_UNBLOCK)
            .playerOnly(true)
            .build();

    public static final CommandDefinition ACCEPT = CommandDefinition.builder(FRIEND.append("accept"))
            .description("Accept a friend request")
            .usage("/friend accept <player>")
            .permission(PermissionHolder.FRIEND_REQUESTS_ACCEPT)
            .playerOnly(true)
            .build();

    public static final CommandDefinition ACCEPT_ALL = CommandDefinition.builder(FRIEND.append("acceptall"))
            .description("Accept all friend requests")
            .usage("/friend acceptall")
            .permission(PermissionHolder.FRIEND_REQUESTS_ACCEPT_ALL)
            .playerOnly(true)
            .build();

    public static final CommandDefinition DENY = CommandDefinition.builder(FRIEND.append("deny"))
            .description("Deny a friend request")
            .usage("/friend deny <player>")
            .permission(PermissionHolder.FRIEND_REQUESTS_DENY)
            .playerOnly(true)
            .build();

    public static final CommandDefinition DENY_ALL = CommandDefinition.builder(FRIEND.append("denyall"))
            .description("Deny all friend requests")
            .usage("/friend denyall")
            .permission(PermissionHolder.FRIEND_REQUESTS_DENY_ALL)
            .playerOnly(true)
            .build();

    public static final CommandDefinition CANCEL = CommandDefinition.builder(FRIEND.append("cancel"))
            .description("Cancel a sent friend request")
            .usage("/friend cancel <player|all>")
            .permission(PermissionHolder.FRIEND_REQUESTS_CANCEL)
            .playerOnly(true)
            .build();

    public static final CommandDefinition REQUESTS = CommandDefinition.builder(FRIEND.append("requests"))
            .description("Shows all pending requests")
            .usage("/friend requests")
            .permission(PermissionHolder.FRIEND_REQUESTS_LIST)
            .playerOnly(true)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition LIST = CommandDefinition.builder(FRIEND.append("list"))
            .description("Shows all your friends")
            .usage("/friend list")
            .permission(PermissionHolder.FRIEND_LIST)
            .playerOnly(true)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition FRIENDS_ALIAS = CommandDefinition.builder(FRIENDS)
            .description("Shows all your friends")
            .usage("/friends")
            .permission(PermissionHolder.FRIEND_LIST)
            .playerOnly(true)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition RELOAD = CommandDefinition.builder(FRIEND.append("reload"))
            .description("Reload FriendNet Configs")
            .usage("/friend reload")
            .permission(PermissionHolder.FRIENDS_RELOAD)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition PROXY = CommandDefinition.builder(FRIEND.append("proxy"))
            .description("Manage FriendNet proxy backend operations")
            .usage("/friend proxy <subcommand>")
            .permission(PermissionHolder.FRIENDS_PROXY)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition PROXY_SYNC = CommandDefinition.builder(PROXY.path().append("sync"))
            .description("Sync backend display names to the proxy")
            .usage("/friend proxy sync")
            .permission(PermissionHolder.FRIENDS_PROXY_SYNC)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition PROXY_HANDSHAKE = CommandDefinition.builder(PROXY.path().append("handshake"))
            .description("Register this backend with the proxy")
            .usage("/friend proxy handshake")
            .permission(PermissionHolder.FRIENDS_PROXY_HANDSHAKE)
            .playerOnly(true)
            .platformSpecific(true)
            .build();

    public static final CommandDefinition PROXY_RELOAD = CommandDefinition.builder(PROXY.path().append("reload"))
            .description("Reload FriendNet proxy configs")
            .usage("/friend proxy reload")
            .permission(PermissionHolder.FRIENDS_PROXY_RELOAD)
            .platformSpecific(true)
            .build();

    private FriendCommandDefinitions() {
    }

    public static List<CommandDefinition> all() {
        return List.of(
                ROOT,
                ADD,
                REMOVE,
                BLOCK,
                UNBLOCK,
                ACCEPT,
                ACCEPT_ALL,
                DENY,
                DENY_ALL,
                CANCEL,
                REQUESTS,
                LIST,
                FRIENDS_ALIAS,
                RELOAD,
                PROXY,
                PROXY_SYNC,
                PROXY_HANDSHAKE,
                PROXY_RELOAD
        );
    }

    public static CommandRegistry registryWithUsageHandlers() {
        CommandRegistry registry = new CommandRegistry();
        for (CommandDefinition definition : all()) {
            registry.register(definition, context -> CommandFeedbackUseCases.usage(
                    CommandUsageFormatter.usage(all(), definition, ignored -> true)
            ));
        }
        return registry;
    }
}
