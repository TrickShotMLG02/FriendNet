package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.constants.FriendNetPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendReloadCommand extends AbstractCommand {

    public FriendReloadCommand(JavaPlugin plugin) {
        super(
                plugin,
                "reload",
                "Reload FriendNet Configs",
                "/friend reload",
                FriendNetPermissions.RELOAD
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        MessageManager.loadMessages();

        MessageManager.send(sender, "configReloadSuccess");
        return true;
    }


    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
