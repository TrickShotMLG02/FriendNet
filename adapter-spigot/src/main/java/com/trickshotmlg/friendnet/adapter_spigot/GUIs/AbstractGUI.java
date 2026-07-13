package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.InteractableItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class AbstractGUI {
    protected final JavaPlugin plugin;
    protected final Player player;
    protected Inventory inventory;
    protected final int size;
    protected final String titleKey;

    protected AbstractGUI parentGUI; // Optional for going back

    protected Map<Integer, InteractableItemStack> interactableSlots = new HashMap<>();

    private static final Map<Player, AbstractGUI> openGUIs = new WeakHashMap<>();

    public AbstractGUI(JavaPlugin plugin, Player player, int size, String titleKey) {
        this.plugin = plugin;
        this.player = player;
        this.size = size;
        this.titleKey = titleKey;
    }

    public static AbstractGUI getOpenGUI(Player player) {
        return openGUIs.get(player);
    }

    /**
     * Function to open the inventory for the player
     */
    public void open() {
        inventory = Bukkit.createInventory(null, size, getInventoryTitle());
        buildInventory(); // fill it with items
        player.openInventory(inventory);
        openGUIs.put(player, this);
    }

    protected String getInventoryTitle() {
        FriendNetPlugin pl = (FriendNetPlugin) plugin;
        return pl.LocaleManager.getMessage(player.getUniqueId(), "gui", titleKey);
    }

    /**
     * Function to open a GUI as a child of the current one
     * @param child
     */
    public void openChild(AbstractGUI child) {
        child.parentGUI = this;
        child.open();
    }

    public void goBack() {
        if (parentGUI != null) {
            parentGUI.open();
        } else {
            close();
        }
    }

    public void close() {
        openGUIs.remove(player);
        player.closeInventory();
    }

    /**
     * Function to populate the inventory with itemStacks
     */
    protected abstract void buildInventory();

    /**
     * Function to handle inventory clicks
     * @param player The player that clicked
     * @param slot The slot number that was clicked
     * @param clicked The ItemStack that was clicked
     */
    public void handleClick(Player player, int slot, ItemStack clicked) {

    }

    /**
     * Function to check if the clicked item is the same as another item
     * @param clicked The ItemStack that was clicked
     * @param toCheck The ItemStack that should be compared (can also be null)
     * @return True if the items match, else false
     */
    protected boolean checkItemClicked(ItemStack clicked, ItemStack toCheck) {
        if (clicked == null || toCheck == null) {
            return false;
        }
        return clicked.equals(toCheck);
    }

    /**
     * Helper Function to create an ItemStack
     * @param material The material to use for the ItemStack
     * @param name The name of the Item
     * @param lore The lore of the item
     * @return The built ItemStack
     */
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Wrapper function to set an interactable item to a slot in the inventory
     * @param slot The slot number to set the item to
     * @param item The InteractableItem to set
     */
    protected void setInteractableItem(int slot, InteractableItemStack item) {
        interactableSlots.put(slot, item);
        inventory.setItem(slot, item.getItemStack());
    }

    public Map<Integer, InteractableItemStack> getInteractableSlots() { return interactableSlots; }
}
