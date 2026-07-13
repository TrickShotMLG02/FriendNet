package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ToggleItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendFilterGUI extends AbstractGUI {
    private static final Map<UUID, FriendFilterState> FILTERS = new ConcurrentHashMap<>();
    private final FriendFilterState filterState;
    private final boolean includeFavoritesOnlyFilter;

    public FriendFilterGUI(JavaPlugin plugin, Player player) {
        this(plugin, player, true);
    }

    public FriendFilterGUI(JavaPlugin plugin, Player player, boolean includeFavoritesOnlyFilter) {
        super(plugin, player, 9 * 5, "titles.friendFilterGUI");
        this.filterState = getFilterState(player);
        this.includeFavoritesOnlyFilter = includeFavoritesOnlyFilter;
    }

    public static FriendFilterState getFilterState(Player player) {
        return FILTERS.computeIfAbsent(player.getUniqueId(), ignored -> new FriendFilterState());
    }

    @Override
    protected void buildInventory() {
        interactableSlots.clear();
        inventory.clear();

        int startCol = includeFavoritesOnlyFilter ? 2 : 1;
        if (includeFavoritesOnlyFilter) {
            addFilterOption(1, startCol, Material.NETHER_STAR, "friendFilterGUI.buttons.favoritesOnly", filterState.isFavoritesOnly(), filterState::setFavoritesOnly);
        }

        addFilterOption(1, startCol + 1, Material.LIME_DYE, "friendFilterGUI.buttons.onlineOnly", filterState.isOnlineOnly(), filterState::setOnlineOnly);
        addNameSearchOption(1, startCol + 2);
        addFilterOption(1, startCol + 3, Material.CLOCK, "friendFilterGUI.buttons.recentlySeen", filterState.isSortByRecentlySeen(), filterState::setSortByRecentlySeen);
        addSortDirectionOption(1, startCol + 4);

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

    private void addNameSearchOption(int row, int col) {
        int labelSlot = 9 * row + col;
        ItemStack label = SpigotUtils.createItem(
                Material.NAME_TAG,
                player,
                "gui",
                "friendFilterGUI.buttons.nameSearch.displayName",
                "friendFilterGUI.buttons.nameSearch.lore"
        );

        appendSearchQueryLore(label);
        inventory.setItem(labelSlot, label);

        setInteractableItem(labelSlot + 9, new ActionItemStack(
                SpigotUtils.createItem(
                        Material.NAME_TAG,
                        filterState.hasNameSearchQuery()
                                ? locale("friendFilterGUI.buttons.nameSearch.active.displayName", Map.of("query", filterState.getNameSearchQuery()))
                                : locale("friendFilterGUI.buttons.nameSearch.search.displayName"),
                        List.of()
                ),
                player,
                () -> openChild(new FriendNameSearchGUI(plugin, player, filterState)),
                ActionItemStack.SoundProfile.NAVIGATION
        ));
    }

    private void addSortDirectionOption(int row, int col) {
        int labelSlot = 9 * row + col;
        inventory.setItem(labelSlot, SpigotUtils.createItem(
                Material.HOPPER,
                player,
                "gui",
                "friendFilterGUI.buttons.sortDirection.displayName",
                "friendFilterGUI.buttons.sortDirection.lore"
        ));

        setInteractableItem(labelSlot + 9, new ToggleItemStack(
                filterState.isReverseSort(),
                Material.SPECTRAL_ARROW,
                Material.ARROW,
                null,
                null,
                player,
                "gui",
                "friendFilterGUI.buttons.sortDirection.ascending.displayName",
                "friendFilterGUI.buttons.sortDirection.descending.displayName",
                "friendFilterGUI.buttons.sortDirection.ascending.lore",
                "friendFilterGUI.buttons.sortDirection.descending.lore",
                filterState::setReverseSort
        ));
    }

    private void appendSearchQueryLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !filterState.hasNameSearchQuery()) {
            return;
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(locale("friendFilterGUI.buttons.nameSearch.current", Map.of("query", filterState.getNameSearchQuery())));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private String locale(String key) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key);
    }

    private String locale(String key, Map<String, Object> placeholders) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key, placeholders);
    }

    private void fillEmptySlots() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }
}
