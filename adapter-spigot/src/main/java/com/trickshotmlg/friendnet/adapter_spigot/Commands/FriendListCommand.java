package com.trickshotmlg.friendnet.adapter_spigot.Commands;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.FriendsGUI;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.command.FriendListViewData;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FriendListCommand extends AbstractCommand {

    public FriendListCommand(JavaPlugin plugin) {
        super(
                plugin,
                "list",
                "Shows all your friends",
                "/friend list",
                PermissionHolder.FRIEND_LIST
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
        FriendListViewData viewData = pl.getApplicationServices()
                .friendCommandUseCases()
                .listViewData(player.getUniqueId());

        // create Friend GUI
        FriendsGUI gui = new FriendsGUI(pl, player, viewData.friends(), viewData.pendingRequests());

        gui.open();

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
