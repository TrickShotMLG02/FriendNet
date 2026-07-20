package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
            SpigotCommandResultRenderer.playersOnly(sender);
            return true;
        }

        if (args.length > 0) {
            SpigotCommandResultRenderer.usage(sender, getUsage());
            return true;
        }

        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();

        SpigotCommandResultRenderer.render(
                sender,
                pl.getApplicationServices().friendCommandUseCases().denyAllRequests(player.getUniqueId())
        );

        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
