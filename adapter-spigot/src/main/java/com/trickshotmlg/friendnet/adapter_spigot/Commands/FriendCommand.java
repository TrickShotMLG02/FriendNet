package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
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
                FriendNetPermissions.FRIEND_USE
        );

        // Register subcommands
        registerSubCommand(new FriendAddCommand(plugin));
        //registerSubCommand(new FriendRequestCommand());

        PluginCommand command = plugin.getCommand("friend");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

       Logger.debug("Registered command: " + command);
    }

    /**
     * @param sender
     * @param args
     * @return
     */
    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(getUsageMessage());
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
