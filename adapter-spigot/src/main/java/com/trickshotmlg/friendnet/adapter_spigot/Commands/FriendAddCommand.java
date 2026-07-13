package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver.KnownPlayerTarget;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();
        BlocklistActions blocklistActions = new BlocklistActions(pl);
        Optional<KnownPlayerTarget> optionalTarget = KnownPlayerResolver.resolve(pl, args[0]);

        if (optionalTarget.isEmpty()) {
            MessageManager.send(sender, "commandFeedback.playerNotFound");
            return true;
        }

        KnownPlayerTarget target = optionalTarget.get();
        var targetId = target.playerId();
        String targetName = target.displayName();

        if (targetId.equals(player.getUniqueId())) {
            MessageManager.send(sender, "friendRequest.send.sender.cannotSelf");
            return true;
        }

        if (blocklistActions.isBlocked(player.getUniqueId(), targetId)) {
            MessageManager.send(sender, "blocklist.friendRequest.senderBlocked", Map.of("target", targetName));
            return true;
        }

        if (blocklistActions.isBlocked(targetId, player.getUniqueId())) {
            MessageManager.send(sender, "blocklist.friendRequest.targetBlocked", Map.of("target", targetName));
            return true;
        }

        PlayerData targetData = target.playerData();

        if (!targetData.isAllowFriendRequests()) {
            MessageManager.send(sender, "friendRequest.send.sender.disabled", Map.of("target", targetName));
            return true;
        }

        boolean success = fs.sendFriendRequest(player.getUniqueId(), targetId);
        if (success) {

            // --- Accept button ---
            TextComponent accept = MessageManager.createButton(
                    targetId,
                    "chatButtons.acceptRequest.text",
                    Map.of(),
                    "chatButtons.acceptRequest.hover",
                    Map.of(),
                    ClickEvent.Action.RUN_COMMAND,
                    "/friend accept " + player.getName()
            );

            // --- Deny button ---
            TextComponent deny = MessageManager.createButton(
                    targetId,
                    "chatButtons.denyRequest.text",
                    Map.of(),
                    "chatButtons.denyRequest.hover",
                    Map.of(),
                    ClickEvent.Action.RUN_COMMAND,
                    "/friend deny " + player.getName()
            );

            if (targetData.isAutoAcceptFriends()) {
                success = fs.acceptFriendRequest(targetId, player.getUniqueId());
                if (success) {
                    MessageManager.send(sender, "friendRequest.send.sender.autoAccepted", Map.of("target", targetName));
                    if (target.onlinePlayer() != null && targetData.isFriendRequestNotifications()) {
                        MessageManager.send(target.onlinePlayer(), "friendRequest.send.target.autoAccepted", Map.of("sender", player.getName()));
                    }
                } else {
                    MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", targetName));
                }
            } else {
                MessageManager.send(sender, "friendRequest.send.sender.success", Map.of("target", targetName));
                if (target.onlinePlayer() != null && targetData.isFriendRequestNotifications()) {
                    MessageManager.send(target.onlinePlayer(), "friendRequest.send.target.success",
                            Map.of(
                                    "sender", player.getName(),
                                    "acceptRequest", accept,
                                    "denyRequest", deny
                            )
                    );
                }
            }
        } else {
            // CODE COPIED! from FriendAcceptCommand.java
            if (fs.requestPending(player.getUniqueId(), targetId)) {
                success = fs.acceptFriendRequest(player.getUniqueId(), targetId);

                if (success) {
                    MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", targetName));
                    if (target.onlinePlayer() != null) {
                        MessageManager.send(target.onlinePlayer(), "friendRequest.accept.target.success", Map.of("sender", player.getName()));
                    }
                } else {
                    MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", targetName));
                }
            } else {
                MessageManager.send(sender, "friendRequest.send.sender.alreadyPending", Map.of("target", targetName));
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
        if (args.length == 1 && sender instanceof Player player) {
            FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
            FriendService fs = pl.getFriendService();
            BlocklistActions blocklistActions = new BlocklistActions(pl);
            String prefix = args[0].toLowerCase();

            return Bukkit.getOnlinePlayers().stream()
                    .filter(candidate -> !candidate.getUniqueId().equals(player.getUniqueId()))
                    .filter(candidate -> !fs.areFriends(player.getUniqueId(), candidate.getUniqueId()))
                    .filter(candidate -> !fs.requestPending(candidate.getUniqueId(), player.getUniqueId()))
                    .filter(candidate -> !fs.requestPending(player.getUniqueId(), candidate.getUniqueId()))
                    .filter(candidate -> !blocklistActions.hasEitherBlocked(player.getUniqueId(), candidate.getUniqueId()))
                    .filter(candidate -> {
                        PlayerData playerData = pl.getPlayerService().getPlayerData(candidate.getUniqueId());
                        return playerData == null || playerData.isAllowFriendRequests();
                    })
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        return List.of();
    }
}
