package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import com.trickshotmlg.friendnet.core_api.interfaces.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class FriendCommand extends BaseCommand {
    public FriendCommand(FriendNetPlugin pluginInstance, String permission) {
        super(pluginInstance, permission);
    }

    /**
     * @param sender Source of the command
     * @param cmd    Command which was executed
     * @param label  Alias of the command which was used
     * @param args   Passed command arguments
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        PlatformPlayer p = new SpigotPlayer(player);
        FriendService service = plugin.getFriendService();

        if (cmd.getName().equalsIgnoreCase("friend")) {
            if (args.length == 0) return false;

            /*
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (!player.hasPermission(FriendNetPermissions.FRIEND_ADD)) {
                        player.sendMessage("§cYou do not have permission to add friends.");
                        return true;
                    }
                    UUID target = ...; // get target UUID
                    service.addFriend(p.getUniqueId(), target);
                    player.sendMessage("§aFriend added!");
                }
                case "remove" -> {
                    if (!player.hasPermission(FriendNetPermissions.FRIEND_REMOVE)) {
                        player.sendMessage("§cYou do not have permission to remove friends.");
                        return true;
                    }
                    UUID target = ...;
                    service.removeFriend(p.getUniqueId(), target);
                    player.sendMessage("§aFriend removed!");
                }
                case "list" -> {
                    if (!player.hasPermission(FriendNetPermissions.FRIEND_LIST)) {
                        player.sendMessage("§cYou do not have permission to view friends.");
                        return true;
                    }
                    Set<UUID> friends = service.getFriends(p.getUniqueId());
                    player.sendMessage("§aFriends: " + friends.size());
                }
            }
            */

        }

        return true;
    }
}
