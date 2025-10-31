package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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
        interactableSlots.clear();
        inventory.clear();

        int startIndex = currentPage * friendsPerPage;
        int endIndex = Math.min(startIndex + friendsPerPage, friends.size());

        List<FriendshipData> visibleFriends = SpigotUtils.safeSubList(friends, startIndex, endIndex);

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
            setInteractableItem(bottomRowStart,
                    new ActionItemStack(
                            GUIUtils.CreatePreviousPageItem(player),
                            player,
                            () -> {
                                if (currentPage > 0) {
                                    currentPage--;
                                    buildInventory();
                                }
                            }
                    )
            );
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVkOWQ1YzJiNDgwNzA1OGQ5ODdjNmUxZDYzMDBhMWNjNGI5ZWVlN2IxNmYxZjBhY2FjMTRmZmNkMWE5Njk5ZiJ9fX0=";
            //inventory.setItem(bottomRowStart, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Next page
        if (endIndex < friends.size()) {
            setInteractableItem(bottomRowStart + 8,
                    new ActionItemStack(
                            GUIUtils.CreateNextPageItem(player),
                            player,
                            () -> {
                                int maxPage = (int) Math.ceil((double) friends.size() / friendsPerPage) - 1;
                                if (currentPage < maxPage) {
                                    currentPage++;
                                    buildInventory();
                                }
                            }
                    )
            );
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3YmFhNDc2NzIzNGMwMWMwNGI4YmJlYjUxOGEwNTNkY2U3MzlmNGEwNDM1OGE0MjQzMDJmYjRhMDE3MmY4In19fQ==";
            //inventory.setItem(bottomRowStart + 8, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Block List Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.BARRIER,
                            player,
                            "gui",
                            "friendsGUI.buttons.blocklist.displayName",
                            "friendsGUI.buttons.blocklist.lore"
                    ),
                    player,
                    () -> {}
            );

            setInteractableItem(bottomRowStart + 3 - 9, actionItemStack);
        }

        // Player Head
        {
            inventory.setItem(
                    bottomRowStart + 4 - 9,
                    SpigotUtils.createPlayerHead(
                            player.getUniqueId(),
                            player.getDisplayName(),
                            List.of("Statistics:", "Total Friends: " + friends.size(), "Total Requests: " + requests.size())
                    )
            );
        }

        // Pending Requests Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.BOOK,
                            player,
                            "gui",
                            "friendsGUI.buttons.requests.displayName",
                            "friendsGUI.buttons.requests.lore"
                    ),
                    player,
                    () -> this.openChild(new RequestsGUI(plugin, player, friends, requests))
            );

            setInteractableItem(bottomRowStart + 5 - 9, actionItemStack);
        }

        // Personal Settings Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.COMPARATOR,
                            player,
                            "gui",
                            "friendsGUI.buttons.personalSettings.displayName",
                            "friendsGUI.buttons.personalSettings.lore"
                    ),
                    player,
                    () -> this.openChild(new PersonalSettingsGUI(plugin, player))
            );

            setInteractableItem(bottomRowStart + 3, actionItemStack);
        }

        // Favorite Friends Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.NETHER_STAR,
                            player,
                            "gui",
                            "friendsGUI.buttons.favorites.displayName",
                            "friendsGUI.buttons.favorites.lore"
                    ),
                    player,
                    () -> {}
            );

            setInteractableItem(bottomRowStart + 6, actionItemStack);
        }

        // Page Display Item
        {
            int maxPage = (int) Math.ceil((float) friends.size() / (float) friendsPerPage);
            inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));
        }

        // Filter Item
        {
            ActionItemStack actionItemStack = new ActionItemStack(
                    SpigotUtils.createItem(
                            Material.HOPPER,
                            player,
                            "gui",
                            "friendsGUI.buttons.filter.displayName",
                            "friendsGUI.buttons.filter.lore"
                    ),
                    player,
                    () -> {}
            );

            setInteractableItem(bottomRowStart + 5, actionItemStack);
        }

        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    /**
     * @param player  The player that clicked
     * @param slot    The slot number that was clicked
     * @param clicked The ItemStack that was clicked
     */
    @Override
    public void handleClick(Player player, int slot, ItemStack clicked) {

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
