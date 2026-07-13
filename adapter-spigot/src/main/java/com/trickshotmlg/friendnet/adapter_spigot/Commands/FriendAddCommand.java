package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
                PermissionHolder.FRIEND_ADD
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
            MessageManager.send(sender, "friendRequest.send.sender.cannotSelf");
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();
        PlayerService ps = pl.getPlayerService();
        BlocklistActions blocklistActions = new BlocklistActions(pl);

        if (blocklistActions.isBlocked(player.getUniqueId(), target.getUniqueId())) {
            MessageManager.send(sender, "blocklist.friendRequest.senderBlocked", Map.of("target", target.getName()));
            return true;
        }

        if (blocklistActions.isBlocked(target.getUniqueId(), player.getUniqueId())) {
            MessageManager.send(sender, "blocklist.friendRequest.targetBlocked", Map.of("target", target.getName()));
            return true;
        }

        if (!ps.getPlayerData(target.getUniqueId()).isAllowFriendRequests()) {
            MessageManager.send(sender, "friendRequest.send.sender.disabled", Map.of("target", target.getName()));
            return true;
        }

        boolean success = fs.sendFriendRequest(player.getUniqueId(), target.getUniqueId());
        if (success) {

            // --- Accept button ---
            TextComponent accept = MessageManager.createButton(
                    target.getUniqueId(),
                    "chatButtons.acceptRequest.text",
                    Map.of(),
                    "chatButtons.acceptRequest.hover",
                    Map.of(),
                    ClickEvent.Action.RUN_COMMAND,
                    "/friend accept " + sender.getName()
            );

            // --- Deny button ---
            TextComponent deny = MessageManager.createButton(
                    target.getUniqueId(),
                    "chatButtons.denyRequest.text",
                    Map.of(),
                    "chatButtons.denyRequest.hover",
                    Map.of(),
                    ClickEvent.Action.RUN_COMMAND,
                    "/friend deny " + sender.getName()
            );


            MessageManager.send(sender, "friendRequest.send.sender.success", Map.of("target", target.getName()));
            MessageManager.send(target, "friendRequest.send.target.success",
                    Map.of(
                            "sender", sender.getName(),
                            "acceptRequest", accept,
                            "denyRequest", deny
                    )
            );
        } else {
            // CODE COPIED! from FriendAcceptCommand.java
            if (fs.requestPending(player.getUniqueId(), target.getUniqueId())) {
                success = fs.acceptFriendRequest(player.getUniqueId(), target.getUniqueId());

                if (success) {
                    MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", target.getName()));
                    MessageManager.send(target, "friendRequest.accept.target.success", Map.of("sender", sender.getName()));
                } else {
                    MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", target.getName()));
                }
            } else {
                MessageManager.send(sender, "friendRequest.send.sender.alreadyPending", Map.of("target", target.getName()));
            }
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
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()) && n != sender.getName())
                    .toList();
        }

        return List.of();
    }
}
