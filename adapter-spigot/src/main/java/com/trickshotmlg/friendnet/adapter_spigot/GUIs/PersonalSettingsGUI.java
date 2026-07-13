package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.PlayerSettingsActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ToggleItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PersonalSettingsGUI extends AbstractGUI {

    private int currentPage = 0;
    private final PlayerService playerService;

    public PersonalSettingsGUI(JavaPlugin plugin, Player player) {
        super(
            plugin,
            player,
            9 * 4,
            FriendNetPlugin.LocaleManager.getMessage(
                    player.getUniqueId(),
                    "gui",
                    "titles.personalSettingsGUI"
            )
        );

        playerService = ((FriendNetPlugin) plugin).getPlayerService();
    }


    @Override
    protected void buildInventory() {
        inventory.clear();

        // Back Item
        {
            setInteractableItem(0,
                    new ActionItemStack(
                            GUIUtils.CreateBackItem(player),
                            player,
                            () -> goBack(),
                            ActionItemStack.SoundProfile.NAVIGATION
                    )
            );
        }

        // Allow Requests Item
        {
            int row = 1;
            int col = 2;
            int slot = 9 * row + col;
            ItemStack allowRequestsItem = SpigotUtils.createItem(
                    Material.BARRIER,
                    player,
                    "gui",
                    "personalSettingsGUI.buttons.toggleAllowRequests.displayName",
                    "personalSettingsGUI.buttons.toggleAllowRequests.lore"
            );
            inventory.setItem(9 * row + col, allowRequestsItem);

            slot += 9;
            setInteractableItem(slot, new ToggleItemStack(
                    playerService.getPlayerData(player.getUniqueId()).isAllowFriendRequests(),
                    player,
                    newState -> new PlayerSettingsActions((FriendNetPlugin) plugin, player).setAllowFriendRequests(newState)
            ));
        }

        // Show Online Status Item
        {
            int row = 1;
            int col = 3;
            int slot = 9 * row + col;
            ItemStack showOnlineStatusItem = SpigotUtils.createItem(
                    Material.BARRIER,
                    player,
                    "gui",
                    "personalSettingsGUI.buttons.toggleShowOnlineStatus.displayName",
                    "personalSettingsGUI.buttons.toggleShowOnlineStatus.lore"
            );
            inventory.setItem(9 * row + col, showOnlineStatusItem);

            slot += 9;
            setInteractableItem(slot, new ToggleItemStack(
                    playerService.getPlayerData(player.getUniqueId()).isShowOnlineStatus(),
                    player,
                    newState -> new PlayerSettingsActions((FriendNetPlugin) plugin, player).setShowOnlineStatus(newState)
            ));
        }

        // Auto Accept Friend Requests Item
        {
            int row = 1;
            int col = 4;
            int slot = 9 * row + col;
            ItemStack autoAcceptFriendsItem = SpigotUtils.createItem(
                    Material.BARRIER,
                    player,
                    "gui",
                    "personalSettingsGUI.buttons.toggleAutoAcceptRequests.displayName",
                    "personalSettingsGUI.buttons.toggleAutoAcceptRequests.lore"
            );
            inventory.setItem(9 * row + col, autoAcceptFriendsItem);

            slot += 9;
            setInteractableItem(slot, new ToggleItemStack(
                    playerService.getPlayerData(player.getUniqueId()).isAutoAcceptFriends(),
                    player,
                    newState -> new PlayerSettingsActions((FriendNetPlugin) plugin, player).setAutoAcceptFriends(newState)
            ));
        }

        // Friend Request Notifications Item
        {
            int row = 1;
            int col = 5;
            int slot = 9 * row + col;
            ItemStack friendRequestNotificationsItem = SpigotUtils.createItem(
                    Material.BARRIER,
                    player,
                    "gui",
                    "personalSettingsGUI.buttons.toggleRequestNotifications.displayName",
                    "personalSettingsGUI.buttons.toggleRequestNotifications.lore"
            );
            inventory.setItem(9 * row + col, friendRequestNotificationsItem);

            slot += 9;
            setInteractableItem(slot, new ToggleItemStack(
                    playerService.getPlayerData(player.getUniqueId()).isFriendRequestNotifications(),
                    player,
                    newState -> new PlayerSettingsActions((FriendNetPlugin) plugin, player).setFriendRequestNotifications(newState)
            ));
        }

        // Public Friend list Item
        {
            int row = 1;
            int col = 6;
            int slot = 9 * row + col;
            ItemStack publicFriendListItem = SpigotUtils.createItem(
                    Material.BARRIER,
                    player,
                    "gui",
                    "personalSettingsGUI.buttons.togglePublicFriendList.displayName",
                    "personalSettingsGUI.buttons.togglePublicFriendList.lore"
            );
            inventory.setItem(9 * row + col, publicFriendListItem);

            slot += 9;
            setInteractableItem(slot, new ToggleItemStack(
                    playerService.getPlayerData(player.getUniqueId()).isFriendListPublic(),
                    player,
                    newState -> new PlayerSettingsActions((FriendNetPlugin) plugin, player).setFriendListPublic(newState)
            ));
        }

        // Locale Selector Item
        {
            int row = 1;
            int col = 7;
            int slot = 9 * row + col;
            ItemStack localeItem = GUIUtils.CreateLocalizedHead(
                    player,
                    GUIUtils.GLOBE_TEXTURE,
                    "gui",
                    "personalSettingsGUI.buttons.setLanguage.displayName",
                    "personalSettingsGUI.buttons.setLanguage.lore"
            );
            inventory.setItem(9 * row + col, localeItem);

            slot += 9;
            ItemStack openLocaleMenuItem = GUIUtils.CreateLocalizedHead(
                    player,
                    GUIUtils.NEXT_PAGE_TEXTURE,
                    "gui",
                    "personalSettingsGUI.buttons.openLanguageMenu.displayName",
                    "personalSettingsGUI.buttons.openLanguageMenu.lore"
            );
            setInteractableItem(slot, new ActionItemStack(
                    openLocaleMenuItem,
                    player,
                    () -> this.openChild(new LocaleSelectionGUI(plugin, player)),
                    ActionItemStack.SoundProfile.NAVIGATION
            ));
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
