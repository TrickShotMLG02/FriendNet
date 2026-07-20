package com.trickshotmlg.friendnet.adapter_velocity.commands;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.Map;

public class FriendNetVelocityCommand implements SimpleCommand {

    private static final String RELOAD_PERMISSION = PermissionHolder.FRIENDS_RELOAD.getPermissionPrefixed();

    private final FriendNetVelocityPlugin plugin;

    public FriendNetVelocityCommand(FriendNetVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] arguments = invocation.arguments();

        if (arguments.length == 1 && "reload".equalsIgnoreCase(arguments[0])) {
            if (!source.hasPermission(RELOAD_PERMISSION)) {
                plugin.getMessageManager().send(source, "noPermission");
                return;
            }

            boolean success = plugin.reloadPluginConfigs();
            plugin.getMessageManager().send(source, success ? "configReloadSuccess" : "configReloadError");
            return;
        }

        plugin.getMessageManager().send(source, "commandFeedback.usage", Map.of("usage", "/friendnet reload"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] arguments = invocation.arguments();
        if (arguments.length == 0) {
            return List.of("reload");
        }
        if (arguments.length == 1 && "reload".startsWith(arguments[0].toLowerCase()) && invocation.source().hasPermission(RELOAD_PERMISSION)) {
            return List.of("reload");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        String[] arguments = invocation.arguments();
        if (arguments.length > 0 && "reload".equalsIgnoreCase(arguments[0])) {
            return invocation.source().hasPermission(RELOAD_PERMISSION);
        }
        return true;
    }
}
