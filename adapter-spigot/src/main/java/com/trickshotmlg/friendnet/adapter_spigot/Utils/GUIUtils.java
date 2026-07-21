package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * A class containing static methods for creating ItemStacks that are shared across multiple GUIs
 */
public final class GUIUtils {
    public static final String PREVIOUS_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    public static final String NEXT_PAGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    public static final String PAGE_INDICATOR_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAxYWZlOTczYzU0ODJmZGM3MWU2YWExMDY5ODgzM2M3OWM0MzdmMjEzMDhlYTlhMWEwOTU3NDZlYzI3NGEwZiJ9fX0=";
    public static final String RED_X_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==";
    public static final String CHECK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0=";
    public static final String BOOKS_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTViZTIyYjVkNGE4NzVkNzdkZjNmNzcxMGZmNDU3OGVmMjc5MzlhOTY4NGNiZGIzZDMxZDk3M2YxNjY4NDkifX19";
    public static final String GLOBE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBhY2EwMTMxNzhhOWY0NzkxM2U4OTRkM2QwYmZkNGIwYjY2MTIwODI1YjlhYWI4YTRkN2Q5YmYwMjQ1YWJmIn19fQ==";
    public static final String EYE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjMjIyMTUwNmRiMjA1MTc2MjcyZDQ4ZDgyNzVkOGRlYzE3NTc3YmMyMTE5ODM5OGE5ZTUxNTlhMDc0MzQifX19";
    public static final String LOCK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE5OWI1ZWUzMjBlNzk5N2Q5MWJiNWY4NjY1ZjNkMzJhZTQ5MjBlMDNjNmIzZDliN2VlY2E2OTcxMTk5OTcifX19";
    public static final String STAR_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJiYTllYWQ5N2M4ZmI0NGIxNTZhZGU5Y2IyMTRlYTkxMjQzNGEyY2M0N2M0ZGVjNTBmMjEwMjFjNzVkZDJkNyJ9fX0=";
    public static final String FILTER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDNjZjVjZDkyYzFiYTBhN2Q3YmIxZjg3MmNjZGI5NTFjYTg5N2QzNDAwNDA0NTdhMzI0MjcxNjA2YzViYmM1NiJ9fX0=";
    public static final String SETTINGS_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjJmNzkwMTZjYWQ4NGQxYWUyMTYwOWM0ODEzNzgyNTk4ZTM4Nzk2MWJlMTNjMTU2ODI3NTJmMTI2ZGNlN2EifX19";
    public static final String UNITED_STATES_FLAG_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNiYzMyY2IyNGQ1N2ZjZGMwMzFlODUxMjM1ZGEyZGFhZDNlMTkxNGI4NzA0M2JkMDEyNjMzZTZmMzJjNyJ9fX0=";
    public static final String GERMANY_FLAG_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjFjMjMwOTdlZjE3NjJkNTFkZjI5N2Y5YzQ0NTcwOGQ5NDM5ZmY5MTc0NmQyZjY3N2IyOGRkZGZhMjczMTYifX19";
    public static final String SWITZERLAND_FLAG_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVjMjdlZDliOGNmNmYzNzgyODE1MGRmMDJjNDg2Nzk5ODlhZTcwODI5ODY2Y2U2OThjMjVhMzdjNjY2ZGUyIn19fQ==";

    private static final Map<String, String> LANGUAGE_FLAG_COUNTRIES = Map.of(
            "en", "US",
            "de", "DE",
            "gsw", "CH"
    );

    private static final Map<String, String> COUNTRY_FLAG_TEXTURES = Map.of(
            "US", UNITED_STATES_FLAG_TEXTURE,
            "DE", GERMANY_FLAG_TEXTURE,
            "CH", SWITZERLAND_FLAG_TEXTURE
    );

    public static int CalculateMaxPage(int itemCount, int itemsPerPage) {
        if (itemsPerPage <= 0) {
            throw new IllegalArgumentException("itemsPerPage must be greater than zero");
        }

        return Math.max(1, (int) Math.ceil((double) itemCount / itemsPerPage));
    }

    public static ItemStack CreateLocalizedHead(Player player, String texture, String type, String displayNameKey, String loreKey) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, displayNameKey);
        List<String> lore = SpigotUtils.parseStringList(
                FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), type, loreKey)
        );

        return SpigotUtils.createCustomPlayerHead(texture, displayName, lore);
    }

    public static String GetLocaleFlagTexture(LocaleKey locale) {
        if (locale == null) {
            return GLOBE_TEXTURE;
        }

        String country = locale.getCountry();
        if (country == null || country.isBlank()) {
            country = LANGUAGE_FLAG_COUNTRIES.get(locale.getLanguage());
        }

        return COUNTRY_FLAG_TEXTURES.getOrDefault(country, GLOBE_TEXTURE);
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

        return SpigotUtils.createCustomPlayerHead(RED_X_TEXTURE, displayName, lore);
    }

    public static ItemStack CreateEmptyStateItem(Player player, String messageKey) {
        String displayName = FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", messageKey);
        return SpigotUtils.createItem(Material.GRAY_DYE, displayName, List.of());
    }
}
