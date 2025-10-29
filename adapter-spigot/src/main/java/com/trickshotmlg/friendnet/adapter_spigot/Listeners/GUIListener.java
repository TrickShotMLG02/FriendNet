package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.GUIs.AbstractGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIListener extends AbstractListener {

    public GUIListener(JavaPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        AbstractGUI gui = AbstractGUI.getOpenGUI(player);
        if (gui != null) {
            event.setCancelled(true); // prevent taking items
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            gui.handleClick(player, slot, clicked);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        AbstractGUI gui = AbstractGUI.getOpenGUI(player);
        if (gui != null) {
            gui.close(); // removes from map
        }
    }
}
