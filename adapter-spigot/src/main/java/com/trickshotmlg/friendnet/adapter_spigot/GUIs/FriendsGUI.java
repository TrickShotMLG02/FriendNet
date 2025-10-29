package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsGUI extends AbstractGUI {

    private final List<FriendshipData> friends;
    private final List<FriendshipData> requests;

    private final int friendRows = 4;

    private int currentPage = 0;
    private final int friendsPerPage = friendRows * 9;

    public FriendsGUI(JavaPlugin plugin, Player player, List<FriendshipData> friends, List<FriendshipData> requests) {
        //super(plugin, player, ((friends.size() - 1) / 9 + 1) * 9, "Your Friends");
        super(plugin, player, 9 * 6, "Your Friends");
        this.friends = friends;
        this.requests = requests;
    }

    @Override
    protected void buildInventory() {
        // Clear previous contents
        inventory.clear();

        int startIndex = currentPage * friendsPerPage;
        int endIndex = Math.min(startIndex + friendsPerPage, friends.size());

        List<FriendshipData> visibleFriends = friends.subList(startIndex, endIndex);

        // Populate friends for this page
        for (int i = 0; i < visibleFriends.size(); i++) {
            FriendshipData friend = visibleFriends.get(i);
            ItemStack friendItem = createFriendItem(friend);
            List<String> lore = List.of(
                    "Status: " + friend.getFriendshipStatus(),
                    "Favourite: " + (friend.isFavourite() ? "Yes" : "No")
            );
            friendItem = SpigotUtils.setItemLore(friendItem, lore);
            inventory.setItem(i, friendItem);
        }

        // Navigation buttons
        int bottomRowStart = inventory.getSize() - 9;

        // Previous page
        if (currentPage > 0) {
            inventory.setItem(bottomRowStart, SpigotUtils.createItem(org.bukkit.Material.ARROW, "§ePrevious Page"));
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVkOWQ1YzJiNDgwNzA1OGQ5ODdjNmUxZDYzMDBhMWNjNGI5ZWVlN2IxNmYxZjBhY2FjMTRmZmNkMWE5Njk5ZiJ9fX0=";
            //inventory.setItem(bottomRowStart, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Next page
        if (endIndex < friends.size()) {
            inventory.setItem(bottomRowStart + 8, SpigotUtils.createItem(org.bukkit.Material.ARROW, "§eNext Page"));
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3YmFhNDc2NzIzNGMwMWMwNGI4YmJlYjUxOGEwNTNkY2U3MzlmNGEwNDM1OGE0MjQzMDJmYjRhMDE3MmY4In19fQ==";
            //inventory.setItem(bottomRowStart + 8, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Block List Item
        inventory.setItem(bottomRowStart + 3 - 9, SpigotUtils.createItem(Material.BARRIER, "§eBlocked"));

        // Player Head
        inventory.setItem(
                bottomRowStart + 4 - 9,
                SpigotUtils.createPlayerHead(
                        player.getUniqueId(),
                        player.getDisplayName(),
                        List.of("Statistics:", "Total Friends: " + friends.size(), "Total Requests: " + requests.size())
                )
        );

        // Pending Requests Item
        inventory.setItem(bottomRowStart + 5 - 9, SpigotUtils.createItem(Material.BOOK, "§ePending Requests"));

        // Personal Settings Item
        inventory.setItem(bottomRowStart + 3, SpigotUtils.createItem(Material.COMPARATOR, "§eSettings"));

        // Page Display Item
        inventory.setItem(bottomRowStart + 4, SpigotUtils.createItem(Material.PAPER, "§7Page " + (currentPage + 1)));

        // Filter Item
        inventory.setItem(bottomRowStart + 5, SpigotUtils.createItem(Material.HOPPER, "§eFilter"));


        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    @Override
    public void handleClick(Player player, int slot, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();
        if (displayName == null) return;

        if (displayName.contains("Previous Page")) {
            if (currentPage > 0) {
                currentPage--;
                buildInventory();
            }
        } else if (displayName.contains("Next Page")) {
            int maxPage = (int) Math.ceil((double) friends.size() / friendsPerPage) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                buildInventory();
            }
        } else {
            // Handle friend item clicks later (open detail GUI, etc.)
        }
    }

    @Deprecated
    private void refreshPage() {
        buildInventory();
        player.updateInventory();
    }

    private ItemStack createFriendItem(FriendshipData friend) {

        UUID friendID = friend.getOtherPlayerId(this.player.getUniqueId());
        String friendName = SpigotUtils.getPlayerDisplayName(friendID);

        return SpigotUtils.createPlayerHead(friendID, friendName, List.of("N/A"));
    }
}
