package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(FriendNetPlugin pluginInstance) {
        super(pluginInstance, FriendNetPermissions.RELOAD);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        Player player = (Player) sender;

        //TODO:
        /*
        if (playerHasPermission(player)) {
            if (plugin.reloadPluginConfigs()) {
                plugin.applyAllValues();
                sendPluginMessage(player, plugin.messages.getString("configReloadSuccess"));
            } else {
                sendPluginMessage(player, plugin.messages.getString("configReloadError"));
            }
        }
        */

        return true;
    }
}