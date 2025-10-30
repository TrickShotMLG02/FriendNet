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

        // TODO: Check for errors while reloading

        // TODO: load config file

        // Load Messages from messages_XX.yml file
        FriendNetPlugin pl = (FriendNetPlugin) getPlugin();
        pl.reloadPluginConfigs();

        // TODO: Eventually reload certain listeners/commands

        MessageManager.send(sender, "configReloadSuccess");
        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
