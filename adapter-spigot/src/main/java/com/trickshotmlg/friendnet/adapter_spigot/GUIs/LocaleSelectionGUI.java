package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Actions.PlayerSettingsActions;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.InteractableItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.RadioItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.LocaleUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.RadioGroup;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocaleSelectionGUI extends AbstractGUI{

    private final List<LocaleKey> availableLocales;

    private int currentPage = 0;
    private final int localesPerPage = 5;
    private final int localesStartIndexOffset = 1 * 9 + 2;

    private final PlayerService playerService;

    public LocaleSelectionGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 9 * 4, "titles.localeSelectionGUI");

        playerService = ((FriendNetPlugin) plugin).getPlayerService();
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        LocaleKey selectedLocale;
        if (pd != null) {
            selectedLocale = pd.getLocale();
        } else {
            selectedLocale = LocaleKey.getDefaultLocale();
        }

        availableLocales = LocaleKey.values().stream().sorted(
                Comparator.comparing(
                        lk -> LocaleUtils.getLocalizedLanguageName(selectedLocale, lk)
                )
        ).toList();

    }

    private LocaleSelectionGUI(JavaPlugin plugin, Player player, int currentPage) {
        this(plugin, player);
        this.currentPage = currentPage;
    }

    @Override
    protected void buildInventory() {
        // Clear previous contents
        interactableSlots.clear();
        inventory.clear();

        int startIndex = currentPage * localesPerPage;
        int endIndex = Math.min(startIndex + localesPerPage, availableLocales.size());

        List<LocaleKey> visibleLocales = SpigotUtils.safeSubList(availableLocales, startIndex, endIndex);

        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        LocaleKey selectedLocale = getSelectedLocale(pd);

        RadioGroup localeSelectionGroup = new RadioGroup(inventory);
        List<InteractableItemStack> localeToggles = new ArrayList<>();

        for (LocaleKey locale : availableLocales) {
            localeToggles.add(
                    new RadioItemStack(
                            localeSelectionGroup,
                            locale.equals(selectedLocale),
                            player,
                            newState -> {
                                new PlayerSettingsActions((FriendNetPlugin) plugin, player).setLocale(locale);

                                // manually set all
                                updateRadioSelection(visibleLocales.size(), localeToggles);

                                reopenWithUpdatedTitle();
                            }
                    )
            );
        }

        // Populate locales for this page
        for (int i = 0; i < visibleLocales.size(); i++) {
            LocaleKey locale = visibleLocales.get(i);

            ItemStack localeItem = SpigotUtils.createCustomPlayerHead(
                    getLocaleTexture(locale),
                    "§e" + LocaleUtils.getLocalizedLanguageName(selectedLocale, locale),
                    null
            );
            inventory.setItem(i + localesStartIndexOffset, localeItem);
            setInteractableItem(i + localesStartIndexOffset + 9, localeToggles.get(i + localesPerPage * currentPage));
        }

        // Navigation buttons
        int bottomRowStart = inventory.getSize() - 9;

        // Previous page
        if (currentPage > 0) {
            setInteractableItem(bottomRowStart,
                    new ActionItemStack(
                            GUIUtils.CreatePreviousPageItem(player),
                            player,
                            () -> {
                                if (currentPage > 0) {
                                    currentPage--;
                                    buildInventory();
                                }
                            }
                    )
            );
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVkOWQ1YzJiNDgwNzA1OGQ5ODdjNmUxZDYzMDBhMWNjNGI5ZWVlN2IxNmYxZjBhY2FjMTRmZmNkMWE5Njk5ZiJ9fX0=";
            //inventory.setItem(bottomRowStart, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Next page
        if (endIndex < availableLocales.size()) {
            setInteractableItem(bottomRowStart + 8,
                    new ActionItemStack(
                            GUIUtils.CreateNextPageItem(player),
                            player,
                            () -> {
                                int maxPage = (int) Math.ceil((double) availableLocales.size() / localesPerPage) - 1;
                                if (currentPage < maxPage) {
                                    currentPage++;
                                    buildInventory();
                                }
                            }
                    )
            );
            //String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3YmFhNDc2NzIzNGMwMWMwNGI4YmJlYjUxOGEwNTNkY2U3MzlmNGEwNDM1OGE0MjQzMDJmYjRhMDE3MmY4In19fQ==";
            //inventory.setItem(bottomRowStart + 8, SpigotUtils.getSkull(texture, "§ePrevious Page", 1));
        }

        // Page Display Item
        {
            int maxPage = GUIUtils.CalculateMaxPage(availableLocales.size(), localesPerPage);
            inventory.setItem(bottomRowStart + 4, GUIUtils.CreatePageIndicatorItem(player, currentPage, maxPage));
        }

        // Back Item
        {
            setInteractableItem(0,
                    new ActionItemStack(
                            GUIUtils.CreateBackItem(player),
                            player,
                            () -> goBack()
                    )
            );
        }

        // Filler for aesthetics
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, SpigotUtils.createFillerGlass());
            }
        }
    }

    /**
     * Function to update the appearance of the InteractableItemStacks to their changed ItemStack
     * @param count The amount of items to update (equal to amount of rendered items in inventory from localeToggles)
     * @param localeToggles The list containing all InteractableItemStacks that can be redrawn
     */
    private void updateRadioSelection(int count, List<InteractableItemStack> localeToggles) {
        for (int i = 0; i < count; i++) {
            setInteractableItem(i + localesStartIndexOffset + 9, localeToggles.get(i + localesPerPage * currentPage));
        }
    }

    private LocaleKey getSelectedLocale(PlayerData playerData) {
        LocaleKey defaultLocale = LocaleKey.getDefaultLocale();
        if (playerData == null || playerData.getLocale() == null) {
            return defaultLocale;
        }

        return playerData.getLocale();
    }

    private String getLocaleTexture(LocaleKey locale) {
        return FriendNetPlugin.LocaleManager.getLocaleString(locale, "gui", "locale.texture")
                .orElseGet(() -> GUIUtils.GetLocaleFlagTexture(locale));
    }

    private void reopenWithUpdatedTitle() {
        LocaleSelectionGUI refreshedGUI = new LocaleSelectionGUI(plugin, player, currentPage);
        refreshedGUI.parentGUI = parentGUI;
        refreshedGUI.open();
    }
}
