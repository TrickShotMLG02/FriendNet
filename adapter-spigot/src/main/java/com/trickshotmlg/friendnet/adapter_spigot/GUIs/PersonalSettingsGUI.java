package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.PlayerSettingsActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ActionItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.GUIs.Items.ToggleItemStack;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiViewData;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.GUIUtils;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.ProxyActionResponseRenderer;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PersonalSettingsGUI extends AbstractGUI {

    private int currentPage = 0;
    private final PlayerService playerService;
    private FriendGuiViewData viewData;

    public PersonalSettingsGUI(JavaPlugin plugin, Player player) {
        this(plugin, player, null);
    }

    public PersonalSettingsGUI(JavaPlugin plugin, Player player, FriendGuiViewData viewData) {
        super(
            plugin,
            player,
            9 * 4,
            "titles.personalSettingsGUI"
        );

        playerService = ((FriendNetPlugin) plugin).getPlayerService();
        this.viewData = viewData;
        applyViewDataToLocalPlayer();
    }


    @Override
    protected void buildInventory() {
        interactableSlots.clear();
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
                    allowFriendRequests(),
                    player,
                    newState -> setBooleanSetting(ProxyActionType.SET_ALLOW_FRIEND_REQUESTS, newState, () ->
                            new PlayerSettingsActions((FriendNetPlugin) plugin, player).setAllowFriendRequests(newState))
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
                    showOnlineStatus(),
                    player,
                    newState -> setBooleanSetting(ProxyActionType.SET_SHOW_ONLINE_STATUS, newState, () ->
                            new PlayerSettingsActions((FriendNetPlugin) plugin, player).setShowOnlineStatus(newState))
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
                    autoAcceptFriends(),
                    player,
                    newState -> setBooleanSetting(ProxyActionType.SET_AUTO_ACCEPT_FRIENDS, newState, () ->
                            new PlayerSettingsActions((FriendNetPlugin) plugin, player).setAutoAcceptFriends(newState))
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
                    friendRequestNotifications(),
                    player,
                    newState -> setBooleanSetting(ProxyActionType.SET_FRIEND_REQUEST_NOTIFICATIONS, newState, () ->
                            new PlayerSettingsActions((FriendNetPlugin) plugin, player).setFriendRequestNotifications(newState))
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
                    friendListPublic(),
                    player,
                    newState -> setBooleanSetting(ProxyActionType.SET_FRIEND_LIST_PUBLIC, newState, () ->
                            new PlayerSettingsActions((FriendNetPlugin) plugin, player).setFriendListPublic(newState))
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
                    () -> this.openChild(new LocaleSelectionGUI(plugin, player, viewData)),
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

    @Override
    protected void updateViewData(FriendGuiViewData viewData) {
        this.viewData = viewData;
    }

    private void setBooleanSetting(ProxyActionType actionType, boolean enabled, Runnable standaloneAction) {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        if (!friendNetPlugin.isProxyBackendMode()) {
            standaloneAction.run();
            return;
        }

        ProxyActionRequestPayload request = new ProxyActionRequestPayload(actionType, null, "", true, enabled);
        friendNetPlugin.getFriendGuiService().executeAction(player, request).whenComplete((response, throwable) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (throwable != null) {
                        MessageManager.send(player, "commandFeedback.proxyBackendGuiUnavailable");
                        return;
                    }

                    if (response.friendListView() != null) {
                        viewData = FriendGuiViewData.fromProxyPayload(player.getUniqueId(), response.friendListView());
                        applyViewDataToLocalPlayer();
                        updateViewDataChain(viewData);
                    }
                    ProxyActionResponseRenderer.render(player, response);
                    buildInventory();
                })
        );
    }

    private boolean allowFriendRequests() {
        return viewData != null ? viewData.allowFriendRequests() : playerData().isAllowFriendRequests();
    }

    private boolean showOnlineStatus() {
        return viewData != null ? viewData.showOnlineStatus() : playerData().isShowOnlineStatus();
    }

    private boolean autoAcceptFriends() {
        return viewData != null ? viewData.autoAcceptFriends() : playerData().isAutoAcceptFriends();
    }

    private boolean friendRequestNotifications() {
        return viewData != null ? viewData.friendRequestNotifications() : playerData().isFriendRequestNotifications();
    }

    private boolean friendListPublic() {
        return viewData != null ? viewData.friendListPublic() : playerData().isFriendListPublic();
    }

    private PlayerData playerData() {
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        return playerData != null ? playerData : new PlayerData(player.getUniqueId());
    }

    private void applyViewDataToLocalPlayer() {
        if (viewData == null) {
            return;
        }

        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        if (playerData == null) {
            playerData = playerService.initPlayer(player.getUniqueId());
        }
        viewData.applySettingsTo(playerData);
    }
}
