package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ToggleItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FriendFilterGUI extends AbstractGUI {

    public FriendFilterGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 5, "titles.friendFilterGUI");
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        addFilterOption(1, 2, Material.NETHER_STAR, "friendFilterGUI.buttons.favoritesOnly");
        addFilterOption(1, 3, Material.LIME_DYE, "friendFilterGUI.buttons.onlineOnly");
        addFilterOption(1, 4, Material.NAME_TAG, "friendFilterGUI.buttons.nameSearch");
        addFilterOption(1, 5, Material.CLOCK, "friendFilterGUI.buttons.recentlySeen");
        addFilterOption(1, 6, Material.HOPPER, "friendFilterGUI.buttons.sortMode");

        int bottomRowStart = inventory.getSize() - 9;

        setInteractableItem(bottomRowStart + 3, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.REDSTONE_BLOCK,
                        player,
                        "gui",
                        "friendFilterGUI.buttons.reset.displayName",
                        "friendFilterGUI.buttons.reset.lore"
                ),
                player,
                () -> {}
        ));

        setInteractableItem(bottomRowStart + 4, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack()
        ));

        setInteractableItem(bottomRowStart + 5, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.EMERALD_BLOCK,
                        player,
                        "gui",
                        "friendFilterGUI.buttons.apply.displayName",
                        "friendFilterGUI.buttons.apply.lore"
                ),
                player,
                () -> {}
        ));

        fillEmptySlots();
    }

    private void addFilterOption(int row, int col, Material material, String keyPrefix) {
        int labelSlot = 9 * row + col;
        inventory.setItem(labelSlot, SpigotUtils.createItem(
                material,
                player,
                "gui",
                keyPrefix + ".displayName",
                keyPrefix + ".lore"
        ));

        setInteractableItem(labelSlot + 9, new ToggleItemStack(false, player, null));
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
