package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendListCommand extends AbstractCommand {

    public FriendListCommand(JavaPlugin plugin) {
        super(
                plugin,
                "list",
                "Shows all your friends",
                "/friend list",
                FriendNetPermissions.FRIEND_LIST
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
