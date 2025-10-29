package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class ReloadCommandOLD extends BaseCommand {

    public ReloadCommandOLD(FriendNetPlugin pluginInstance) {
        super(pluginInstance, "");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        Player player = (Player) sender;

        if (playerHasPermission(player)) {
            if (plugin.reloadPluginConfigs()) {
                //plugin.applyAllValues();
                sendPluginMessage(player, plugin.messages.getString("configReloadSuccess"));
            } else {
                sendPluginMessage(player, plugin.messages.getString("configReloadError"));
            }
        }

        return true;
    }
}