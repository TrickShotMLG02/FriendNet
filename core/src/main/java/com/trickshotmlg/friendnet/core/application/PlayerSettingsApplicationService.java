package com.trickshotmlg.friendnet.core.application;

import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerSettingsApplicationService {

    private final PlayerService playerService;
    private final Consumer<UUID> dirtyMarker;
    private final FriendStatusVisibilityNotifier statusNotifier;

    public PlayerSettingsApplicationService(
            PlayerService playerService,
            Consumer<UUID> dirtyMarker,
            FriendStatusVisibilityNotifier statusNotifier
    ) {
        this.playerService = playerService;
        this.dirtyMarker = dirtyMarker != null ? dirtyMarker : ignored -> {
        };
        this.statusNotifier = statusNotifier != null ? statusNotifier : FriendStatusVisibilityNotifier.NONE;
    }

    public boolean setAllowFriendRequests(UUID playerId, boolean allowFriendRequests) {
        return update(playerId, playerData -> playerData.setAllowFriendRequests(allowFriendRequests));
    }

    public boolean setAutoAcceptFriends(UUID playerId, boolean autoAcceptFriends) {
        return update(playerId, playerData -> playerData.setAutoAcceptFriends(autoAcceptFriends));
    }

    public boolean setFriendRequestNotifications(UUID playerId, boolean friendRequestNotifications) {
        return update(playerId, playerData -> playerData.setFriendRequestNotifications(friendRequestNotifications));
    }

    public boolean setFriendListPublic(UUID playerId, boolean friendListPublic) {
        return update(playerId, playerData -> playerData.setFriendListPublic(friendListPublic));
    }

    public boolean setLocale(UUID playerId, LocaleKey locale) {
        return update(playerId, playerData -> playerData.setLocale(locale));
    }

    public boolean setShowOnlineStatus(UUID playerId, boolean showOnlineStatus) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            return false;
        }

        boolean changed = playerData.isShowOnlineStatus() != showOnlineStatus;
        boolean wasVisibleOnline = playerService.isOnline(playerId);
        playerData.setShowOnlineStatus(showOnlineStatus);

        if (changed) {
            if (showOnlineStatus) {
                playerData.setLastSeen();
                playerService.setOnline(playerId, true);
                statusNotifier.notifyOnline(playerId);
            } else {
                if (wasVisibleOnline) {
                    playerData.setLastSeen();
                }
                playerService.setOnline(playerId, false);
                if (wasVisibleOnline) {
                    statusNotifier.notifyOffline(playerId);
                }
            }
        }

        dirtyMarker.accept(playerId);
        return true;
    }

    private boolean update(UUID playerId, PlayerDataUpdater updater) {
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            return false;
        }

        updater.update(playerData);
        dirtyMarker.accept(playerId);
        return true;
    }

    @FunctionalInterface
    private interface PlayerDataUpdater {
        void update(PlayerData playerData);
    }
}
