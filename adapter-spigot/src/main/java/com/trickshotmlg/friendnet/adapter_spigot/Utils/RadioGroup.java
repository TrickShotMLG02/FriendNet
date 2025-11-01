package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.RadioItemStack;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class RadioGroup {
    private final List<RadioItemStack> items = new ArrayList<>();

    private final Inventory inventory;

    public RadioGroup(Inventory inventory) {
        this.inventory = inventory;
    }

    public void add(RadioItemStack item) {
        items.add(item);
    }

    public void select(RadioItemStack selected) {
        for (RadioItemStack item : items) {
            item.setSelected(item == selected);
            // TODO: replace item in inventory
        }
    }

    /**
     * Iterates over all RadioItemStacks and selects the first one
     * for which the given predicate returns true.
     *
     * @param condition Predicate to test each RadioItemStack
     * @return true if a matching item was found and selected, false otherwise
     */
    public boolean selectFirstMatching(Predicate<RadioItemStack> condition) {
        for (RadioItemStack item : items) {
            if (condition.test(item)) {
                select(item);
                return true;
            }
        }
        return false;
    }

    public void clear() {
        items.clear();
    }
}