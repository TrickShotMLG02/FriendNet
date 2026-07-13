package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ToggleItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendFilterGUI extends AbstractGUI {
    private static final Map<UUID, FriendFilterState> FILTERS = new ConcurrentHashMap<>();
    private final FriendFilterState filterState;

    public FriendFilterGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 5, "titles.friendFilterGUI");
        filterState = getFilterState(player);
    }

    public static FriendFilterState getFilterState(Player player) {
        return FILTERS.computeIfAbsent(player.getUniqueId(), ignored -> new FriendFilterState());
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        addFilterOption(1, 2, Material.NETHER_STAR, "friendFilterGUI.buttons.favoritesOnly", filterState.isFavoritesOnly(), filterState::setFavoritesOnly);
        addFilterOption(1, 3, Material.LIME_DYE, "friendFilterGUI.buttons.onlineOnly", filterState.isOnlineOnly(), filterState::setOnlineOnly);
        addFilterOption(1, 4, Material.NAME_TAG, "friendFilterGUI.buttons.nameSearch", filterState.isSortByName(), filterState::setSortByName);
        addFilterOption(1, 5, Material.CLOCK, "friendFilterGUI.buttons.recentlySeen", filterState.isSortByRecentlySeen(), filterState::setSortByRecentlySeen);
        addFilterOption(1, 6, Material.HOPPER, "friendFilterGUI.buttons.sortMode", filterState.isReverseSort(), filterState::setReverseSort);

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
                () -> {
                    filterState.reset();
                    buildInventory();
                }
        ));

        setInteractableItem(bottomRowStart + 4, new ActionItemStack(
                GUIUtils.CreateBackItem(player),
                player,
                () -> goBack(),
                ActionItemStack.SoundProfile.NAVIGATION
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
                () -> goBack(),
                ActionItemStack.SoundProfile.NAVIGATION
        ));

        fillEmptySlots();
    }

    private void addFilterOption(int row, int col, Material material, String keyPrefix, boolean initialState, java.util.function.Consumer<Boolean> onToggle) {
        int labelSlot = 9 * row + col;
        inventory.setItem(labelSlot, SpigotUtils.createItem(
                material,
                player,
                "gui",
                keyPrefix + ".displayName",
                keyPrefix + ".lore"
        ));

        setInteractableItem(labelSlot + 9, new ToggleItemStack(initialState, player, onToggle));
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
