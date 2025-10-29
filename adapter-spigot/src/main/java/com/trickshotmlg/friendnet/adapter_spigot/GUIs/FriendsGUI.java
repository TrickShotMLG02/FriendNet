package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsGUI extends AbstractGUI {

    private final List<FriendshipData> friends;

    public FriendsGUI(JavaPlugin plugin, Player player, List<FriendshipData> friends) {
        //super(plugin, player, ((friends.size() - 1) / 9 + 1) * 9, "Your Friends");
        super(plugin, player, 9 * 6, "Your Friends");
        this.friends = friends;
    }

    @Override
    protected void buildInventory() {
        // Clear previous contents
        inventory.clear();

        for (int i = 0; i < friends.size(); i++) {
            FriendshipData friend = friends.get(i);
            ItemStack friendItem = createFriendItem(friend);

            // Optionally, add lore for status/favourite
            List<String> lore = new ArrayList<>();
            lore.add("Status: " + friend.getFriendshipStatus());
            lore.add("Favourite: " + (friend.isFavourite() ? "Yes" : "No"));
            ItemStack itemWithLore = SpigotUtils.setItemLore(friendItem, lore);

            // Place in the inventory slot
            inventory.setItem(i, itemWithLore);
        }

        // Optionally, fill remaining empty slots with filler glass
        for (int i = friends.size(); i < inventory.getSize(); i++) {
            inventory.setItem(i, SpigotUtils.createFillerGlass());
        }
    }

    @Override
    public void handleClick(Player player, int slot, ItemStack clicked) {

    }

    private ItemStack createFriendItem(FriendshipData friend) {

        UUID friendID = friend.getOtherPlayerId(this.player.getUniqueId());
        String friendName = SpigotUtils.getPlayerDisplayName(friendID);

        return SpigotUtils.createPlayerHead(friendID, friendName, List.of("N/A"));
    }
}
