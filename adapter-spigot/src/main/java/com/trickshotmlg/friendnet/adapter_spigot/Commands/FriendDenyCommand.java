package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendDenyCommand extends AbstractCommand{

    protected FriendDenyCommand(JavaPlugin plugin) {
        super(
                plugin,
                "deny",
                "Deny a friend request from a player",
                "/friend deny <player>",
                FriendNetPermissions.FRIEND_DENY
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
