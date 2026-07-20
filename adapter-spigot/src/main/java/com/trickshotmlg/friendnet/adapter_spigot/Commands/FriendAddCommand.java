package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver.KnownPlayerTarget;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
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
            SpigotCommandResultRenderer.playersOnly(sender);
            return true;
        }

        if (args.length < 1) {
            SpigotCommandResultRenderer.usage(sender, getUsage());
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        FriendService fs = pl.getFriendService();
        Optional<KnownPlayerTarget> optionalTarget = KnownPlayerResolver.resolve(pl, args[0]);

        if (optionalTarget.isEmpty()) {
            SpigotCommandResultRenderer.playerNotFound(sender);
            return true;
        }

        KnownPlayerTarget target = optionalTarget.get();
        var targetId = target.playerId();
        String targetName = target.displayName();
        PlayerData targetData = target.playerData();

        SpigotCommandResultRenderer.render(
                sender,
                pl.getApplicationServices().friendCommandUseCases().sendFriendRequest(
                        player.getUniqueId(),
                        player.getName(),
                        new KnownPlayerLookup.KnownPlayer(targetId, targetName, targetData, target.onlinePlayer() != null)
                )
        );

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
