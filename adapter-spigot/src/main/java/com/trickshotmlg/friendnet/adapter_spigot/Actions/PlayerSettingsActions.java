package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Services.PlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.entity.Player;

public class PlayerSettingsActions {

    private final PlayerService playerService;
    private final PlayerDataSaveQueue playerDataSaveQueue;
    private final Player player;

    public PlayerSettingsActions(FriendNetPlugin plugin, Player player) {
        this.playerService = plugin.getPlayerService();
        this.playerDataSaveQueue = plugin.getPlayerDataSaveQueue();
        this.player = player;
    }

    public boolean setAllowFriendRequests(boolean allowFriendRequests) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setAllowFriendRequests(allowFriendRequests);
            completeSettingsChange("playerSettings.allowFriendRequests", allowFriendRequests);
            return true;
        }

        return false;
    }

    public boolean setShowOnlineStatus(boolean showOnlineStatus) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setShowOnlineStatus(showOnlineStatus);
            completeSettingsChange("playerSettings.showOnlineStatus", showOnlineStatus);
            return true;
        }

        return false;
    }

    public boolean setAutoAcceptFriends(boolean autoAcceptFriends) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setAutoAcceptFriends(autoAcceptFriends);
            completeSettingsChange("playerSettings.autoAcceptFriends", autoAcceptFriends);
            return true;
        }

        return false;
    }

    public boolean setFriendRequestNotifications(boolean friendRequestNotifications) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setFriendRequestNotifications(friendRequestNotifications);
            completeSettingsChange("playerSettings.friendRequestNotifications", friendRequestNotifications);
            return true;
        }

        return false;
    }

    public boolean setFriendListPublic(boolean friendListPublic) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setFriendListPublic(friendListPublic);
            completeSettingsChange("playerSettings.friendListPublic", friendListPublic);
            return true;
        }

        return false;
    }

    public boolean setLocale(LocaleKey locale) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setLocale(locale);
            markDirty();
            MessageManager.send(player, "playerSettings.locale.changed");
            return true;
        }

        return false;
    }

    private void markDirty() {
        if (playerDataSaveQueue != null) {
            playerDataSaveQueue.markDirty(player.getUniqueId());
        }
    }

    private void completeSettingsChange(String messageKeyPrefix, boolean enabled) {
        markDirty();
        MessageManager.send(player, messageKeyPrefix + (enabled ? ".enabled" : ".disabled"));
    }
}
