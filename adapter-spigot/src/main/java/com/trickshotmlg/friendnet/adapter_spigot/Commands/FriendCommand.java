package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class FriendCommand extends AbstractCommand {
    public FriendCommand(JavaPlugin plugin) {
        super(
                plugin,
                "friend",
                "Manage your friends",
                "/friend <subcommand>",
                FriendNetPermissions.FRIEND_USE
        );

        // Register subcommands
        registerSubCommand(new FriendAddCommand(plugin));
        registerSubCommand(new FriendRemoveCommand(plugin));
        registerSubCommand(new FriendAcceptCommand(plugin));
        registerSubCommand(new FriendDenyCommand(plugin));

        registerSubCommand(new FriendRequestsCommand(plugin));
        registerSubCommand(new FriendListCommand(plugin));

        registerSubCommand(new FriendReloadCommand(plugin));


        PluginCommand command = plugin.getCommand("friend");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    /**
     * @param sender
     * @param args
     * @return
     */
    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        MessageManager.send(sender, "commandFeedback.usage", Map.of("usage", getUsageMessage()));
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
