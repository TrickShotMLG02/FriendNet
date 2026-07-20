package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendCommand extends AbstractCommand {
    public FriendCommand(JavaPlugin plugin) {
        super(
                plugin,
                "friend",
                "Manage your friends",
                "/friend <subcommand>",
                PermissionHolder.FRIENDS
        );

        // Register subcommands
        registerSubCommand(new FriendAddCommand(plugin));
        registerSubCommand(new FriendRemoveCommand(plugin));
        registerSubCommand(new FriendBlockCommand(plugin));
        registerSubCommand(new FriendUnblockCommand(plugin));
        registerSubCommand(new FriendAcceptCommand(plugin));
        registerSubCommand(new FriendAcceptAllCommand(plugin));
        registerSubCommand(new FriendDenyCommand(plugin));
        registerSubCommand(new FriendDenyAllCommand(plugin));
        registerSubCommand(new FriendCancelCommand(plugin));


        registerSubCommand(new FriendRequestsCommand(plugin));
        registerSubCommand(new FriendListCommand(plugin));

        registerSubCommand(new FriendReloadCommand(plugin));


        PluginCommand command = plugin.getCommand("friend");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

        PluginCommand friendsCommand = plugin.getCommand("friends");
        if (friendsCommand != null) {
            FriendListCommand friendsAliasCommand = new FriendListCommand(plugin);
            friendsCommand.setExecutor(friendsAliasCommand);
            friendsCommand.setTabCompleter(friendsAliasCommand);
        }
    }

    /**
     * @param sender
     * @param args
     * @return
     */
    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        SpigotCommandResultRenderer.usage(sender, getUsageMessage(sender));
        return true;
    }

    /**
     * @param sender
     * @param args
     * @return
     */
    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        //return getSubCommands().stream().map(c -> c.getName()).toList();
        return List.of();
    }
}
