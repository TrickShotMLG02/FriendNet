package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class FriendAddCommand extends AbstractCommand {

    protected FriendAddCommand(JavaPlugin plugin) {
        super(
                plugin,
                "add",
                "Send a friend request to a player",
                "/friend add <player>",
                FriendNetPermissions.FRIEND_ADD
        );
    }

    /**
     * @param sender
     * @param args
     * @return
     */
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

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            MessageManager.send(sender, "commandFeedback.playerNotFound");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageManager.send(sender, "requests.invalidTargetSelf");
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();
        boolean success = fs.sendFriendRequest(player.getUniqueId(), target.getUniqueId());
        if (success) {
            MessageManager.send(sender, "requests.notificationSentRequest", Map.of("target", target.getName()));
            MessageManager.send(target, "requests.notificationReceivedRequest", Map.of("target", sender.getName()));
        } else {
            MessageManager.send(sender, "requests.alreadyPendingRequest", Map.of("target", target.getName()));
        }

        return true;
    }

    /**
     * @param sender
     * @param args
     * @return
     */
    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        // TODO: remove sender from auto-complete list
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()) && n != sender.getName())
                    .toList();
        }

        return List.of();
    }
}
