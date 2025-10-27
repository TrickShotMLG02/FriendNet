package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendCancelCommand extends AbstractCommand{

    protected FriendCancelCommand(JavaPlugin plugin) {
        super(
                plugin,
                "cancel",
                "Cancel a friend request sent to a player",
                "/friend cancel <player>",
                FriendNetPermissions.FRIEND_CANCEL
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
