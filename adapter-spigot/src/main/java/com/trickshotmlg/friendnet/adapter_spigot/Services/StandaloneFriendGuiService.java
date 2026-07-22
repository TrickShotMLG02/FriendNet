package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core.application.command.FriendListViewData;
import com.trickshotmlg.friendnet.core.application.proxy.ProxyActionDispatcher;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendEntry;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StandaloneFriendGuiService implements FriendGuiService {

    private final FriendNetPlugin plugin;
    private final ProxyActionDispatcher actionDispatcher;

    public StandaloneFriendGuiService(FriendNetPlugin plugin) {
        this.plugin = plugin;
        this.actionDispatcher = new ProxyActionDispatcher(
                plugin.getApplicationServices().friendCommandUseCases(),
                plugin.getApplicationServices().playerSettingsService(),
                plugin.getApplicationServices().knownPlayerLookup(),
                this::friendListPayload
        );
    }

    @Override
    public CompletableFuture<FriendGuiViewData> friendListView(Player player) {
        return friendListView(player, player.getUniqueId());
    }

    @Override
    public CompletableFuture<FriendGuiViewData> friendListView(Player player, UUID viewedPlayerId) {
        FriendListViewData viewData = plugin.getApplicationServices()
                .friendCommandUseCases()
                .listViewData(viewedPlayerId);
        PlayerData viewedData = playerData(viewedPlayerId);
        return CompletableFuture.completedFuture(FriendGuiViewData.local(
                viewData.friends(),
                viewData.pendingRequests(),
                plugin.getFriendService().getSentRequests(player.getUniqueId()).stream().toList(),
                plugin.getApplicationServices().blocklistService().getBlockedPlayers(player.getUniqueId()),
                viewedPlayerId,
                plugin.getApplicationServices().knownPlayerLookup().displayName(viewedPlayerId),
                viewedData != null ? viewedData.getFirstSeen() : null,
                viewedData == null || viewedData.isFriendListPublic()
        ));
    }

    @Override
    public CompletableFuture<ProxyActionResponsePayload> executeAction(Player player, ProxyActionRequestPayload actionRequest) {
        return CompletableFuture.completedFuture(actionDispatcher.dispatch(player.getUniqueId(), player.getName(), actionRequest));
    }

    private ProxyFriendListViewPayload friendListPayload(UUID playerId) {
        FriendListViewData viewData = plugin.getApplicationServices()
                .friendCommandUseCases()
                .listViewData(playerId);
        PlayerData viewerData = playerData(playerId);
        return new ProxyFriendListViewPayload(
                toFriendEntries(playerId, viewData.friends()),
                toRequestEntries(playerId, viewData.pendingRequests()),
                toRequestEntries(playerId, plugin.getFriendService().getSentRequests(playerId).stream().toList()),
                plugin.getApplicationServices().blocklistService().getBlockedPlayers(playerId).stream()
                        .map(this::toBlockedEntry)
                        .toList(),
                viewerData == null || viewerData.isAllowFriendRequests(),
                viewerData == null || viewerData.isShowOnlineStatus(),
                viewerData != null && viewerData.isAutoAcceptFriends(),
                viewerData == null || viewerData.isFriendRequestNotifications(),
                viewerData != null && viewerData.isFriendListPublic(),
                viewerData != null && viewerData.getLocale() != null
                        ? viewerData.getLocale().getCode()
                        : defaultLocaleCode(),
                viewerData != null ? toMillis(viewerData.getFirstSeen()) : -1L,
                playerId,
                plugin.getApplicationServices().knownPlayerLookup().displayName(playerId),
                viewerData != null ? toMillis(viewerData.getFirstSeen()) : -1L,
                viewerData == null || viewerData.isFriendListPublic()
        );
    }

    private String defaultLocaleCode() {
        LocaleKey defaultLocale = LocaleKey.getDefaultLocale();
        return defaultLocale != null ? defaultLocale.getCode() : "en_US";
    }

    private List<ProxyFriendEntry> toFriendEntries(UUID viewerId, List<FriendEntry> friends) {
        return friends.stream()
                .map(friend -> toEntry(
                        friend.friendId(),
                        friend.favourite(),
                        toMillis(friend.getRequestSentTime()),
                        toMillis(friend.getFriendSince()),
                        -1L
                ))
                .toList();
    }

    private List<ProxyFriendEntry> toRequestEntries(UUID viewerId, List<FriendshipData> friendships) {
        return friendships.stream()
                .map(friendship -> toEntry(
                        friendship.getOtherPlayerId(viewerId),
                        false,
                        toMillis(friendship.getRequestSentTime()),
                        toMillis(friendship.getFriendSince()),
                        -1L
                ))
                .toList();
    }

    private ProxyFriendEntry toBlockedEntry(BlocklistData blockedPlayer) {
        return toEntry(blockedPlayer.getBlockedId(), false, -1L, -1L, toMillis(blockedPlayer.getBlockedAt()));
    }

    private ProxyFriendEntry toEntry(
            UUID playerId,
            boolean favourite,
            long requestSentTimeMillis,
            long friendSinceMillis,
            long blockedAtMillis
    ) {
        PlayerData playerData = playerData(playerId);
        return new ProxyFriendEntry(
                playerId,
                plugin.getApplicationServices().knownPlayerLookup().displayName(playerId),
                playerData != null && playerData.getSkinTexture() != null ? playerData.getSkinTexture() : "",
                playerData != null && playerData.getSkinSignature() != null ? playerData.getSkinSignature() : "",
                plugin.getPlayerService().isOnline(playerId),
                "",
                favourite,
                requestSentTimeMillis,
                friendSinceMillis,
                blockedAtMillis,
                playerData != null ? toMillis(playerData.getLastSeen()) : -1L
        );
    }

    private PlayerData playerData(UUID playerId) {
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        if (playerData == null) {
            playerData = plugin.getDatabaseService()
                    .find(playerId, PlayerData.class)
                    .orElse(null);
            if (playerData != null) {
                plugin.getPlayerService().putPlayerData(playerData);
            }
        }
        return playerData;
    }

    private long toMillis(Timestamp timestamp) {
        return timestamp == null ? -1L : timestamp.getTime();
    }
}
