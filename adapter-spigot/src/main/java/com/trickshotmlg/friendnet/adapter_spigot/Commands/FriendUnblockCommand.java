package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver.KnownPlayerTarget;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

public class FriendUnblockCommand extends AbstractCommand {

    protected FriendUnblockCommand(JavaPlugin plugin) {
        super(
                plugin,
                "unblock",
                "Unblock a player",
                "/friend unblock <player>",
                PermissionHolder.FRIEND_UNBLOCK
        );
    }

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
        Optional<KnownPlayerTarget> target = KnownPlayerResolver.resolve(pl, args[0]);
        if (target.isEmpty()) {
            SpigotCommandResultRenderer.playerNotFound(sender);
            return true;
        }

        SpigotCommandResultRenderer.render(
                sender,
                pl.getApplicationServices().friendCommandUseCases().unblockPlayer(
                        player.getUniqueId(),
                        target.get().playerId(),
                        target.get().displayName()
                )
        );
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
            String prefix = args[0].toLowerCase();

            return pl.getApplicationServices().blocklistService().getBlockedPlayers(player.getUniqueId()).stream()
                    .map(BlocklistData::getBlockedId)
                    .map(playerId -> KnownPlayerResolver.displayName(pl, playerId))
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        return List.of();
    }
}
