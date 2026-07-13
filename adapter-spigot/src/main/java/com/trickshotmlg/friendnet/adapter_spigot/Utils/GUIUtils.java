package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * A class containing static methods for creating ItemStacks that are shared across multiple GUIs
 */
public final class GUIUtils {
    private static final String PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private static final String NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private static final String PAGE_INDICATOR_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAxYWZlOTczYzU0ODJmZGM3MWU2YWExMDY5ODgzM2M3OWM0MzdmMjEzMDhlYTlhMWEwOTU3NDZlYzI3NGEwZiJ9fX0=";
    private static final String CLOSE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==";

    public static int CalculateMaxPage(int itemCount, int itemsPerPage) {
        if (itemsPerPage <= 0) {
            throw new IllegalArgumentException("itemsPerPage must be greater than zero");
        }

        return Math.max(1, (int) Math.ceil((double) itemCount / itemsPerPage));
    }

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

        return SpigotUtils.createCustomPlayerHead(PREVIOUS_PAGE_TEXTURE, displayName, lore);
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

        return SpigotUtils.createCustomPlayerHead(NEXT_PAGE_TEXTURE, displayName, lore);
    }

    public static ItemStack CreatePageIndicatorItem(Player player, int currentPage, int maxPage) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(
                player.getUniqueId(),
                "gui",
                "pagination.pageIndicator.displayName",
                Map.of("current", currentPage + 1, "max", maxPage)
        );

        return SpigotUtils.createCustomPlayerHead(PAGE_INDICATOR_TEXTURE, displayName, null);
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

        return SpigotUtils.createCustomPlayerHead(PREVIOUS_PAGE_TEXTURE, displayName, lore);
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

        return SpigotUtils.createCustomPlayerHead(CLOSE_TEXTURE, displayName, lore);
    }
}
