package com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items;

import org.bukkit.inventory.ItemStack;

public abstract class InteractableItemStack {

    ItemStack itemStack;

    InteractableItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Gets the ItemStack of this interactable item stack
     * @return The actual ItemStack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Called when a player clicks this item in the GUI.
     * Each subclass implements its own behavior.
     */
    public abstract void onClick();

    /**
     * Optional method to update the ItemStack's appearance.
     * Useful for toggles or dynamic items.
     */
    public void refresh() {
        // Default: do nothing. Subclasses override if needed.
    }
}
