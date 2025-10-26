package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public abstract class BaseCommand implements CommandExecutor {

    FriendNetPlugin plugin;
    String permission;
    final String noPermissionMessageKey = "noPermission";

    public BaseCommand(FriendNetPlugin pluginInstance, String permission) {
        this.plugin = pluginInstance;
        this.permission = permission;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        return true;
    }

    public boolean playerHasPermission(Player player) {
        if (player.hasPermission(permission)) {
            return true;
        } else {
            sendNoPermsMessage(player);
            return false;
        }
    }

    public void sendNoPermsMessage(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messages.getString(noPermissionMessageKey)));
    }

    public void sendPluginMessage(Player player, String message)
    {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}