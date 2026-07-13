package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BlocklistGUI extends AbstractGUI {

    public BlocklistGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 4, "titles.blocklistGUI");
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        int bottomRowStart = inventory.getSize() - 9;

        inventory.setItem(11, SpigotUtils.createItem(
                Material.BARRIER,
                player,
                "gui",
                "blocklistGUI.buttons.blockedPlayers.displayName",
                "blocklistGUI.buttons.blockedPlayers.lore"
        ));

        setInteractableItem(13, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.NAME_TAG,
                        player,
                        "gui",
                        "blocklistGUI.buttons.addBlockedPlayer.displayName",
                        "blocklistGUI.buttons.addBlockedPlayer.lore"
                ),
                player,
                () -> {}
        ));

        setInteractableItem(15, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.LAVA_BUCKET,
                        player,
                        "gui",
                        "blocklistGUI.buttons.clearBlocklist.displayName",
                        "blocklistGUI.buttons.clearBlocklist.lore"
                ),
                player,
                () -> {}
        ));

        setInteractableItem(bottomRowStart + 4, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack(),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
