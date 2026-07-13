package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

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

public class FavoriteFriendsGUI extends AbstractGUI {

    private final int friendRows = 3;
    private final int friendsPerPage = friendRows * 9;
    private int currentPage = 0;

    public FavoriteFriendsGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 5, "titles.favoriteFriendsGUI");
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        List<FriendshipData> favoriteFriends = ((FriendNetPlugin) plugin)
                .getFriendService()
                .getFriendships(player.getUniqueId())
                .stream()
                .filter(FriendshipData::isFavourite)
                .toList();

        int startIndex = currentPage * friendsPerPage;
        int endIndex = Math.min(startIndex + friendsPerPage, favoriteFriends.size());
        List<FriendshipData> visibleFriends = SpigotUtils.safeSubList(favoriteFriends, startIndex, endIndex);

        for (int i = 0; i < visibleFriends.size(); i++) {
            FriendshipData friend = visibleFriends.get(i);
            inventory.setItem(i, createFriendItem(friend));
        }

        int bottomRowStart = inventory.getSize() - 9;

        if (currentPage > 0) {
            setInteractableItem(bottomRowStart, new ActionItemStack(
                    GUIUtils.CreatePreviousPageItem(player),
                    player,
                    () -> {
                        currentPage--;
                        buildInventory();
                    }
            ));
        }

        if (endIndex < favoriteFriends.size()) {
            setInteractableItem(bottomRowStart + 8, new ActionItemStack(
                    GUIUtils.CreateNextPageItem(player),
                    player,
                    () -> {
                        int maxPage = GUIUtils.CalculateMaxPage(favoriteFriends.size(), friendsPerPage) - 1;
                        if (currentPage < maxPage) {
                            currentPage++;
                            buildInventory();
                        }
                    }
            ));
        }

        setInteractableItem(bottomRowStart + 3, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.HOPPER,
                        player,
                        "gui",
                        "favoriteFriendsGUI.buttons.sortFavorites.displayName",
                        "favoriteFriendsGUI.buttons.sortFavorites.lore"
                ),
                player,
                () -> {}
        ));

        int maxPage = GUIUtils.CalculateMaxPage(favoriteFriends.size(), friendsPerPage);
        inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));

        setInteractableItem(bottomRowStart + 5, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack()
        ));

        fillEmptySlots();
    }

    private ItemStack createFriendItem(FriendshipData friend) {
        UUID friendId = friend.getOtherPlayerId(player.getUniqueId());
        String friendName = SpigotUtils.getPlayerDisplayName((FriendNetPlugin) plugin, friendId);
        return SpigotUtils.createPlayerHead(friendId, friendName, List.of("Favourite: Yes"));
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
