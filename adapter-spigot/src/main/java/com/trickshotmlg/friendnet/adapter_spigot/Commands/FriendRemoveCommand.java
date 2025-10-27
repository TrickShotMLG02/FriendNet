package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
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

public class FriendRemoveCommand extends AbstractCommand{

    protected FriendRemoveCommand(JavaPlugin plugin) {
        super(
                plugin,
                "remove",
                "Remove an existing friend",
                "/friend remove <player>",
                FriendNetPermissions.FRIEND_REMOVE
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageManager.send(sender, "commandFeedback.playersOnlyCommand");

            return true;
        }

        if (args.length < 1) {
            MessageManager.send(sender, "commandFeedback.usage", Map.of("usage", getUsage()));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            MessageManager.send(sender, "commandFeedback.playerNotFound");
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();
        boolean success = fs.removeFriend(player.getUniqueId(), target.getUniqueId());

        if (success) {
            MessageManager.send(sender, "friend.removeSuccess", Map.of("target", target.getName()));
        } else {
            MessageManager.send(sender, "friend.removeFail", Map.of("target", target.getName()));
        }

        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
            FriendService fs = pl.getFriendService();
            Set<FriendshipData> friends = fs.getFriendships(player.getUniqueId());

            return friends.stream()
                    .map(f -> Bukkit.getOfflinePlayer(f.getOtherPlayerId(player.getUniqueId())).getName())
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }

}
