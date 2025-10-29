package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SpigotUtils {
    private SpigotUtils() {
        // prevent instantiation
    }

    /**
     * Get online Player from UUID.
     */
    public static Optional<Player> getOnlinePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return Optional.ofNullable(player);
    }

    /**
     * Get OfflinePlayer from UUID (may be online or offline).
     */
    public static OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * Get a player's display name if online, otherwise fall back to name or empty string.
     */
    public static String getPlayerDisplayName(UUID uuid) {
        return getOnlinePlayer(uuid)
                .map(Player::getDisplayName)
                .orElseGet(() -> getOfflinePlayer(uuid).getName() != null ? getOfflinePlayer(uuid).getName() : "");
    }

    /**
     * Get a player's name if online, otherwise fall back to offline player name.
     */
    public static String getPlayerName(UUID uuid) {
        return getOnlinePlayer(uuid)
                .map(Player::getName)
                .orElseGet(() -> getOfflinePlayer(uuid).getName() != null ? getOfflinePlayer(uuid).getName() : "");
    }

    /**
     * Check if a player is online by UUID.
     */
    public static boolean isOnline(UUID uuid) {
        return getOnlinePlayer(uuid).isPresent();
    }

    /**
     * Try to get a UUID from a username. This only works for players who have joined before.
     */
    public static Optional<UUID> getUUIDFromName(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player != null) {
            return Optional.of(player.getUniqueId());
        }
        return Optional.empty();
    }

    /**
     * Safely cast a CommandSender to Player if possible.
     */
    public static Optional<Player> asPlayer(org.bukkit.command.CommandSender sender) {
        if (sender instanceof Player player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    /**
     * Creates a player head item for a specific player.
     *
     * @param playerId UUID of the player whose skin to use
     * @param displayName Display name of the item
     * @param lore Lore lines
     * @param amount Item stack amount
     * @return ItemStack of a player head
     */
    public static ItemStack createPlayerHead(UUID playerId, String displayName, List<String> lore, int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            // Set the owning player for the head
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
            meta.setOwningPlayer(player);

            // Set display name and lore
            if (displayName != null) meta.setDisplayName(displayName);
            if (lore != null && !lore.isEmpty()) meta.setLore(lore);

            head.setItemMeta(meta);
        }

        return head;
    }

    // Optional overload for single amount (default to 1)
    public static ItemStack createPlayerHead(UUID playerId, String displayName, List<String> lore) {
        return createPlayerHead(playerId, displayName, lore, 1);
    }

    public static ItemStack setItemLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createFillerGlass() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        return glass;
    }
}
