package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver.KnownPlayerTarget;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FriendDenyCommand extends AbstractCommand{

    protected FriendDenyCommand(JavaPlugin plugin) {
        super(
                plugin,
                "deny",
                "Deny a friend request from a player",
                "/friend deny <player>",
                PermissionHolder.FRIEND_REQUESTS_DENY
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
                pl.getApplicationServices().friendCommandUseCases().denyRequest(
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
            FriendService fs = pl.getFriendService();
            Set<FriendshipData> requests = fs.getPendingRequests(player.getUniqueId());

            return requests.stream()
                    .map(f -> KnownPlayerResolver.displayName(pl, f.getRequesterId()))
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        return List.of();
    }
}
