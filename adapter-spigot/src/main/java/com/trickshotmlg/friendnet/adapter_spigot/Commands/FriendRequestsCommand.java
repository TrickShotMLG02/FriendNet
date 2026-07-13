package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.FriendsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.RequestsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class FriendRequestsCommand extends AbstractCommand{

    protected FriendRequestsCommand(JavaPlugin plugin) {
        super(
                plugin,
                "requests",
                "Shows all pending requests",
                "/friend requests",
                PermissionHolder.FRIEND_REQUESTS_LIST
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageManager.send(sender, "commandFeedback.playersOnlyCommand");
            return true;
        }

        if (args.length > 0) {
            MessageManager.send(sender, "commandFeedback.usage", Map.of("usage", getUsage()));
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();
        List<FriendshipData> friends = fs.getFriendships(player.getUniqueId()).stream().toList();
        List<FriendshipData> requests = fs.getPendingRequests(player.getUniqueId()).stream().toList();

        new RequestsGUI(getPlugin(), player).openWithParent(new FriendsGUI(pl, player, friends, requests));
        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
