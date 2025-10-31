package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
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

public class FriendDenyAllCommand extends AbstractCommand{

    protected FriendDenyAllCommand(JavaPlugin plugin) {
        super(
                plugin,
                "denyall",
                "Deny all friend requests",
                "/friend denyall",
                PermissionHolder.FRIEND_REQUESTS_DENY_ALL
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

            boolean success = fs.denyFriendRequest(player.getUniqueId(), target.getUniqueId());
            if (success) {
                MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", target.getName()));
            }
            else {
                MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
            FriendService fs = pl.getFriendService();
            Set<FriendshipData> requests = fs.getPendingRequests(player.getUniqueId());

            return requests.stream()
                    .map(f -> Bukkit.getOfflinePlayer(f.getRequesterId()).getName())
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
