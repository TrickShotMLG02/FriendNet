package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver.KnownPlayerTarget;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FriendRemoveCommand extends AbstractCommand{

    protected FriendRemoveCommand(JavaPlugin plugin) {
        super(
                plugin,
                "remove",
                "Remove an existing friend",
                "/friend remove <player>",
                PermissionHolder.FRIEND_REMOVE
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

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        Optional<KnownPlayerTarget> target = KnownPlayerResolver.resolve(pl, args[0]);
        if (target.isEmpty()) {
            MessageManager.send(sender, "commandFeedback.playerNotFound");
            return true;
        }

        FriendService fs = pl.getFriendService();
        boolean success = fs.removeFriend(player.getUniqueId(), target.get().playerId());

        if (success) {
            MessageManager.send(sender, "friend.remove.sender.success", Map.of("target", target.get().displayName()));
        } else {
            MessageManager.send(sender, "friend.remove.sender.notFound", Map.of("target", target.get().displayName()));
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
                    .map(f -> KnownPlayerResolver.displayName(pl, f.getOtherPlayerId(player.getUniqueId())))
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        return List.of();
    }

}
