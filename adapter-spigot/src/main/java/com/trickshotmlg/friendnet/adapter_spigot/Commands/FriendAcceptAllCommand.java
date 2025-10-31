package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FriendAcceptAllCommand extends AbstractCommand {

    protected FriendAcceptAllCommand(JavaPlugin plugin) {
        super(
                plugin,
                "acceptall",
                "Accepts all friend requests",
                "/friend acceptall",
                PermissionHolder.FRIEND_REQUESTS_ACCEPT_ALL
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

        Set<FriendshipData> requests = fs.getPendingRequests(player.getUniqueId());

        for(FriendshipData r : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.getRequesterId());

            boolean success = fs.acceptFriendRequest(player.getUniqueId(), target.getUniqueId());

            if (success) {
                MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", target.getName()));
                MessageManager.send(target, "friendRequest.accept.target.success", Map.of("sender", sender.getName()));
            } else {
                MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
