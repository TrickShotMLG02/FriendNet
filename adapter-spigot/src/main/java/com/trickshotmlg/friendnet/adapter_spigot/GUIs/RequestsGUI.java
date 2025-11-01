package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.FriendRequestActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
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

public class RequestsGUI extends AbstractGUI {
    private final int friendRows = 4;

    private int currentPage = 0;
    private final int requestsPerPage = friendRows * 9;

    public RequestsGUI(JavaPlugin plugin, Player player) {
        super(
                plugin,
                player,
                9 * 6,
                "titles.friendRequestsGUI"
        );
    }

    @Override
    protected void buildInventory() {
        // Clear previous contents
        interactableSlots.clear();
        inventory.clear();

        List<FriendshipData> requests = ((FriendNetPlugin) plugin).getFriendService().getPendingRequests(player.getUniqueId()).stream().toList();

        int startIndex = currentPage * requestsPerPage;
        int endIndex = Math.min(startIndex + requestsPerPage, requests.size());


        List<FriendshipData> visibleRequests = SpigotUtils.safeSubList(requests, startIndex, endIndex);

        // Populate friends for this page
        for (int i = 0; i < visibleRequests.size(); i++) {
            FriendshipData friend = visibleRequests.get(i);
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
        if (endIndex < requests.size()) {

            setInteractableItem(bottomRowStart + 8,
                    new ActionItemStack(
                            GUIUtils.CreateNextPageItem(player),
                            player,
                            () -> {
                                int maxPage = (int) Math.ceil((double) requests.size() / requestsPerPage) - 1;
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

        // Page Display Item
        {
            int maxPage = (int) Math.ceil((float) requests.size() / (float) requestsPerPage);
            inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));
        }

        // Deny All Item
        {
            ItemStack denyAllItem = SpigotUtils.createItem(
                    Material.RED_WOOL,
                    player,
                    "gui",
                    "friendRequestsGUI.buttons.denyAllRequests.displayName",
                    "friendRequestsGUI.buttons.denyAllRequests.lore"
            );
            setInteractableItem(bottomRowStart + 3,
                    new ActionItemStack(
                            denyAllItem,
                            player,
                            () -> {
                                new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService())
                                        .denyAllRequests(player);

                                buildInventory();
                            }
                    )
            );
        }

        // Page Display Item
        //inventory.setItem(bottomRowStart + 4, SpigotUtils.createItem(Material.PAPER, "§7Page " + (currentPage + 1)));

        // Back Item
        {
            setInteractableItem(bottomRowStart + 4,
                    new ActionItemStack(
                            GUIUtils.CreateBackItem(player),
                            player,
                            () -> goBack()
                    )
            );
        }

        // Accept All Item
        {
            ItemStack acceptAllItem = SpigotUtils.createItem(
                    Material.LIME_WOOL,
                    player,
                    "gui",
                    "friendRequestsGUI.buttons.acceptAllRequests.displayName",
                    "friendRequestsGUI.buttons.acceptAllRequests.lore"
            );
            setInteractableItem(bottomRowStart + 5,
                    new ActionItemStack(
                            acceptAllItem,
                            player,
                            () -> {
                                new FriendRequestActions(((FriendNetPlugin) plugin).getFriendService())
                                        .acceptAllRequests(player);

                                buildInventory();
                            }
                    )
            );
        }

        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
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
