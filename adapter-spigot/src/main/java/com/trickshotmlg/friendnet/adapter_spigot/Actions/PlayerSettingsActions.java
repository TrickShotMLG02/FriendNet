package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.application.PlayerSettingsApplicationService;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import org.bukkit.entity.Player;

public class PlayerSettingsActions {

    private final PlayerSettingsApplicationService settingsService;
    private final Player player;

    public PlayerSettingsActions(FriendNetPlugin plugin, Player player) {
        this.settingsService = plugin.getApplicationServices().playerSettingsService();
        this.player = player;
    }

    public boolean setAllowFriendRequests(boolean allowFriendRequests) {
        if (settingsService.setAllowFriendRequests(player.getUniqueId(), allowFriendRequests)) {
            completeSettingsChange("playerSettings.allowFriendRequests", allowFriendRequests);
            return true;
        }

        return false;
    }

    public boolean setShowOnlineStatus(boolean showOnlineStatus) {
        if (settingsService.setShowOnlineStatus(player.getUniqueId(), showOnlineStatus)) {
            completeSettingsChange("playerSettings.showOnlineStatus", showOnlineStatus);
            return true;
        }

        return false;
    }

    public boolean setAutoAcceptFriends(boolean autoAcceptFriends) {
        if (settingsService.setAutoAcceptFriends(player.getUniqueId(), autoAcceptFriends)) {
            completeSettingsChange("playerSettings.autoAcceptFriends", autoAcceptFriends);
            return true;
        }

        return false;
    }

    public boolean setFriendRequestNotifications(boolean friendRequestNotifications) {
        if (settingsService.setFriendRequestNotifications(player.getUniqueId(), friendRequestNotifications)) {
            completeSettingsChange("playerSettings.friendRequestNotifications", friendRequestNotifications);
            return true;
        }

        return false;
    }

    public boolean setFriendListPublic(boolean friendListPublic) {
        if (settingsService.setFriendListPublic(player.getUniqueId(), friendListPublic)) {
            completeSettingsChange("playerSettings.friendListPublic", friendListPublic);
            return true;
        }

        return false;
    }

    public boolean setLocale(LocaleKey locale) {
        if (settingsService.setLocale(player.getUniqueId(), locale)) {
            MessageManager.send(player, "playerSettings.locale.changed");
            return true;
        }

        return false;
    }

    private void completeSettingsChange(String messageKeyPrefix, boolean enabled) {
        MessageManager.send(player, messageKeyPrefix + (enabled ? ".enabled" : ".disabled"));
    }
}
