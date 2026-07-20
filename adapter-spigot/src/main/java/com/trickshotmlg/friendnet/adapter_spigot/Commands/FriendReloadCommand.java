package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendReloadCommand extends AbstractCommand {

    public FriendReloadCommand(JavaPlugin plugin) {
        super(
                plugin,
                "reload",
                "Reload FriendNet Configs",
                "/friend reload",
                PermissionHolder.FRIENDS_RELOAD
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        if (!pl.reloadPluginConfigs()) {
            MessageManager.send(sender, "configReloadError");
            return true;
        }

        MessageManager.send(sender, "configReloadSuccess");
        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
