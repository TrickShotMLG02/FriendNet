package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.database.SQLTables;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
        Logger.info(SQLTables.TABLE_CREATE_PLAYERDATA);
        Logger.info(SQLTables.TABLE_CREATE_FRIENDSHIPS);
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly Players can use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§eUsage: " + getUsage());
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage("§cYou cannot add yourself!.");
            return true;
        }

        sender.sendMessage("§aYou added " + target.getName() + " as a friend!");
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
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
