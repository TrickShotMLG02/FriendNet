package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendRequestsCommand extends AbstractCommand{

    protected FriendRequestsCommand(JavaPlugin plugin) {
        super(
                plugin,
                "requests",
                "Shows all pending requests",
                "/friend requests",
                FriendNetPermissions.FRIEND_REQUESTS
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly Players can use this command!");
            return true;
        }

        if (args.length > 0) {
            sender.sendMessage("§eUsage: " + getUsage());
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();

        sender.sendMessage("§eYou have pending friend requests from the following players:");

        for (FriendshipData data : fs.getPendingRequests(((Player) sender).getUniqueId())) {
            sender.sendMessage("§e" + Bukkit.getOfflinePlayer(data.getRequesterId()).getName());
        }


        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
