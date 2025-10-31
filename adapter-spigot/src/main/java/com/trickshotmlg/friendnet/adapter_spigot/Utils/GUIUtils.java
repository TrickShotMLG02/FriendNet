package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * A class containing static methods for creating ItemStacks that are shared across multiple GUIs
 */
public final class GUIUtils {

    public static ItemStack CreatePreviousPageItem(Player player) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "pagination.previousPage.displayName"
        );
        List<String> lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "pagination.previousPage.lore"
                )
        );

        return SpigotUtils.createItem(org.bukkit.Material.ARROW, displayName, lore);
    }

    public static ItemStack CreateNextPageItem(Player player) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "pagination.nextPage.displayName"
        );
        List<String> lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "pagination.nextPage.lore"
                )
        );

        return SpigotUtils.createItem(org.bukkit.Material.ARROW, displayName, lore);
    }

    public static ItemStack CreatePageIndicatorItem(Player player, int currentPage, int maxPage) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "pagination.pageIndicator.displayName",
                Map.of("current", currentPage + 1, "max", maxPage)
        );

        return SpigotUtils.createItem(Material.PAPER, displayName);
    }

    public static ItemStack CreateBackItem(Player player) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "buttons.back.displayName"
        );
        List<String> lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "buttons.back.lore"
                )
        );

        return SpigotUtils.createItem(Material.SPRUCE_DOOR, displayName, lore);
    }

    public static ItemStack CreateCloseItem(Player player) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "buttons.close.displayName"
        );
        List<String> lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(
                        player.getUniqueId(),
                        "gui",
                        "buttons.close.lore"
                )
        );

        return SpigotUtils.createItem(Material.BARRIER, displayName, lore);
    }
}
