package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.entity.Player;

// TODO: Send player messages about settings change
public class PlayerSettingsActions {

    private final PlayerService playerService;
    private final Player player;

    public PlayerSettingsActions(PlayerService playerService, Player player) {
        this.playerService = playerService;
        this.player = player;
    }

    public boolean setAllowFriendRequests(boolean allowFriendRequests) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setAllowFriendRequests(allowFriendRequests);
            return true;
        }

        return false;
    }

    public boolean setShowOnlineStatus(boolean showOnlineStatus) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setShowOnlineStatus(showOnlineStatus);
            return true;
        }

        return false;
    }

    public boolean setAutoAcceptFriends(boolean autoAcceptFriends) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setAutoAcceptFriends(autoAcceptFriends);
            return true;
        }

        return false;
    }

    public boolean setFriendRequestNotifications(boolean friendRequestNotifications) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setFriendRequestNotifications(friendRequestNotifications);
            return true;
        }

        return false;
    }

    public boolean setFriendListPublic(boolean friendListPublic) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setFriendListPublic(friendListPublic);
            return true;
        }

        return false;
    }

    public boolean setLocale(LocaleKey locale) {
        PlayerData pd = playerService.getPlayerData(player.getUniqueId());
        if (pd != null) {
            pd.setLocale(locale);
            return true;
        }

        return false;
    }
}
