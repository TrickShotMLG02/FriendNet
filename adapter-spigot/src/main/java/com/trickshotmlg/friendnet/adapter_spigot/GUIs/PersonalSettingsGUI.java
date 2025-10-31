package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ToggleItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PersonalSettingsGUI extends AbstractGUI {

    // ItemStacks that have an associated Action
    ItemStack backItem;
    ItemStack allowRequestsItem;
    ItemStack onlineStatusItem;
    ItemStack autoAcceptItem;
    ItemStack requestNotificationsItems;
    ItemStack friendListPublicItem;
    ItemStack languageItem;

    // Other values
    private int currentPage = 0;

    public PersonalSettingsGUI(JavaPlugin plugin, Player player) {
        super(
            plugin,
            player,
            9 * 4,
            FriendNetPlugin.LocaleManager.getMessage(
                    player.getUniqueId(),
                    "gui",
                    "titles.friendRequestsGUI"
            )
        );
    }


    @Override
    protected void buildInventory() {
        inventory.clear();

        // Back item
        {
            backItem = GUIUtils.CreateBackItem(player);
            inventory.setItem(0, backItem);
        }

        // Allow Requests Item
        {
            int row = 1;
            int col = 2;
            allowRequestsItem = SpigotUtils.createItem(
                    Material.BARRIER,
                    player,
                    "gui",
                    "friendsGUI.buttons.blocklist.displayName",
                    "friendsGUI.buttons.blocklist.lore"
            );
            inventory.setItem(9 * row + col, backItem);
        }

        {
            int row = 2;
            int col = 2;
            int slot = 9 * row + col;

            setInteractableItem(slot, new ToggleItemStack(player, newState -> player.sendMessage("Toggle is now " + (newState ? "ON" : "OFF"))));
        }

        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    @Override
    public void handleClick(Player player, int slot, ItemStack clicked) {

    }
}
