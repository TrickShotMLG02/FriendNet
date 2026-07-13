package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
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
import java.util.UUID;

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
        PlayerService ps = pl.getPlayerService();
        DatabaseService databaseService = pl.getDatabaseService();
        BlocklistActions blocklistActions = new BlocklistActions(pl);
        Optional<KnownFriendTarget> optionalTarget = resolveKnownTarget(args[0], ps, databaseService);

        if (optionalTarget.isEmpty()) {
            MessageManager.send(sender, "commandFeedback.playerNotFound");
            return true;
        }

        KnownFriendTarget target = optionalTarget.get();
        UUID targetId = target.playerData().getPlayerId();
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

    private Optional<KnownFriendTarget> resolveKnownTarget(String name, PlayerService playerService, DatabaseService databaseService) {
        Player onlineTarget = Bukkit.getPlayerExact(name);
        if (onlineTarget != null) {
            PlayerData playerData = playerService.getPlayerData(onlineTarget.getUniqueId());
            if (playerData == null) {
                playerData = databaseService.find(onlineTarget.getUniqueId(), PlayerData.class)
                        .orElseGet(() -> playerService.initPlayer(onlineTarget.getUniqueId()));
                playerData.setLastDisplayName(onlineTarget.getDisplayName());
                playerService.putPlayerData(playerData);
            }
            return Optional.of(new KnownFriendTarget(onlineTarget, playerData, onlineTarget.getName()));
        }

        return databaseService.findPlayerByLastDisplayName(name)
                .map(playerData -> {
                    playerService.putPlayerData(playerData);
                    return new KnownFriendTarget(null, playerData, displayNameOrFallback(playerData));
                });
    }

    private String displayNameOrFallback(PlayerData playerData) {
        String displayName = playerData.getLastDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }

        return playerData.getPlayerId().toString();
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

    private record KnownFriendTarget(Player onlinePlayer, PlayerData playerData, String displayName) {
    }
}
