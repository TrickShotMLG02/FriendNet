package com.trickshotmlg.friendnet.adapter_velocity;

import com.trickshotmlg.friendnet.adapter_velocity.services.VelocityPlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_velocity.utils.VelocityFriendStatusNotifier;
import com.trickshotmlg.friendnet.core.application.BlocklistApplicationService;
import com.trickshotmlg.friendnet.core.application.FriendRequestApplicationService;
import com.trickshotmlg.friendnet.core.application.FriendStatusVisibilityNotifier;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core.application.PlayerSettingsApplicationService;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core.application.command.FriendListViewData;
import com.trickshotmlg.friendnet.core.application.proxy.ProxyActionDispatcher;
import com.trickshotmlg.friendnet.core_api.models.FriendEntry;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VelocityApplicationServices {

    private final FriendNetVelocityPlugin plugin;
    private final KnownPlayerLookup knownPlayerLookup;
    private final BlocklistApplicationService blocklistService;
    private final FriendRequestApplicationService friendRequestService;
    private final PlayerSettingsApplicationService playerSettingsService;
    private final FriendCommandUseCases friendCommandUseCases;
    private final ProxyActionDispatcher proxyActionDispatcher;

    public VelocityApplicationServices(FriendNetVelocityPlugin plugin) {
        this.plugin = plugin;
        this.knownPlayerLookup = createKnownPlayerLookup(plugin);
        this.blocklistService = createBlocklistService(plugin, knownPlayerLookup);
        this.friendRequestService = createFriendRequestService(plugin, blocklistService);
        this.playerSettingsService = createPlayerSettingsService(plugin);
        this.friendCommandUseCases = createFriendCommandUseCases(plugin);
        this.proxyActionDispatcher = createProxyActionDispatcher(plugin);
    }

    protected KnownPlayerLookup createKnownPlayerLookup(FriendNetVelocityPlugin plugin) {
        return new KnownPlayerLookup(plugin.getPlatform(), plugin.getDatabaseService(), plugin.getPlayerService());
    }

    protected BlocklistApplicationService createBlocklistService(
            FriendNetVelocityPlugin plugin,
            KnownPlayerLookup knownPlayerLookup
    ) {
        return new BlocklistApplicationService(plugin.getDatabaseService(), plugin.getFriendService(), knownPlayerLookup);
    }

    protected FriendRequestApplicationService createFriendRequestService(
            FriendNetVelocityPlugin plugin,
            BlocklistApplicationService blocklistService
    ) {
        return new FriendRequestApplicationService(plugin.getFriendService(), blocklistService);
    }

    protected PlayerSettingsApplicationService createPlayerSettingsService(FriendNetVelocityPlugin plugin) {
        VelocityPlayerDataSaveQueue playerDataSaveQueue = plugin.getPlayerDataSaveQueue();
        FriendStatusVisibilityNotifier statusNotifier = new FriendStatusVisibilityNotifier() {
            @Override
            public void notifyOnline(java.util.UUID playerId) {
                updatePresenceVisibility(plugin, playerId, true);
                VelocityFriendStatusNotifier.notifyOnline(plugin, playerId);
            }

            @Override
            public void notifyOffline(java.util.UUID playerId) {
                updatePresenceVisibility(plugin, playerId, false);
                VelocityFriendStatusNotifier.notifyOffline(plugin, playerId);
            }
        };

        return new PlayerSettingsApplicationService(
                plugin.getPlayerService(),
                playerId -> {
                    if (playerDataSaveQueue != null) {
                        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
                        if (playerData != null) {
                            playerDataSaveQueue.markDirty(playerData);
                        } else {
                            playerDataSaveQueue.markDirty(playerId);
                        }
                    }
                },
                statusNotifier
        );
    }

    private void updatePresenceVisibility(FriendNetVelocityPlugin plugin, UUID playerId, boolean visibleOnline) {
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        plugin.getNetworkAuthorityService().getPresence(playerId)
                .ifPresentOrElse(
                        presence -> plugin.getNetworkAuthorityService().setPresence(
                                presence.withVisibleOnline(visibleOnline)
                                        .withLastSeen(playerData != null ? playerData.getLastSeen() : presence.lastSeen())
                        ),
                        () -> plugin.getServer().getPlayer(playerId).ifPresent(player -> plugin.getNetworkAuthorityService().setPresence(
                                new NetworkPlayerPresence(
                                        playerId,
                                        player.getUsername(),
                                        player.getUsername(),
                                        player.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse(null),
                                        true,
                                        visibleOnline,
                                        playerData != null ? playerData.getLastSeen() : null
                                )
                        ))
                );
    }

    protected FriendCommandUseCases createFriendCommandUseCases(FriendNetVelocityPlugin plugin) {
        return new FriendCommandUseCases(
                plugin.getFriendService(),
                friendRequestService,
                blocklistService,
                knownPlayerLookup,
                plugin.getDatabaseService()
        );
    }

    protected ProxyActionDispatcher createProxyActionDispatcher(FriendNetVelocityPlugin plugin) {
        return new ProxyActionDispatcher(
                friendCommandUseCases,
                playerSettingsService,
                knownPlayerLookup,
                this::friendListViewPayload
        );
    }

    public ProxyFriendListViewPayload friendListViewPayload(UUID viewerId) {
        FriendListViewData viewData = friendCommandUseCases.listViewData(viewerId);
        PlayerData viewerData = plugin.getPlayerService().getPlayerData(viewerId);
        return new ProxyFriendListViewPayload(
                toFriendEntries(viewerId, viewData.friends()),
                toRequestEntries(viewerId, viewData.pendingRequests()),
                toRequestEntries(viewerId, plugin.getFriendService().getSentRequests(viewerId).stream().toList()),
                blocklistService.getBlockedPlayers(viewerId).stream()
                        .map(blockedPlayer -> toEntry(
                                blockedPlayer.getBlockedId(),
                                false,
                                -1L,
                                -1L,
                                toMillis(blockedPlayer.getBlockedAt())
                        ))
                        .toList(),
                viewerData == null || viewerData.isAllowFriendRequests(),
                viewerData == null || viewerData.isShowOnlineStatus(),
                viewerData != null && viewerData.isAutoAcceptFriends(),
                viewerData == null || viewerData.isFriendRequestNotifications(),
                viewerData != null && viewerData.isFriendListPublic(),
                viewerData != null && viewerData.getLocale() != null
                        ? viewerData.getLocale().getCode()
                        : defaultLocaleCode()
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

    private ProxyFriendEntry toEntry(
            UUID playerId,
            boolean favourite,
            long requestSentTimeMillis,
            long friendSinceMillis,
            long blockedAtMillis
    ) {
        Optional<NetworkPlayerPresence> presence = plugin.getNetworkAuthorityService().getPresence(playerId);
        String displayName = presence
                .map(NetworkPlayerPresence::displayName)
                .filter(name -> !name.isBlank())
                .orElseGet(() -> knownPlayerLookup.displayName(playerId));
        boolean online = presence
                .map(status -> status.online() && status.visibleOnline())
                .orElse(false);
        String serverName = presence
                .map(NetworkPlayerPresence::serverName)
                .orElse("");
        PlayerData playerData = plugin.getPlayerService().getPlayerData(playerId);
        long lastSeenMillis = playerData != null ? toMillis(playerData.getLastSeen()) : -1L;

        return new ProxyFriendEntry(
                playerId,
                displayName,
                online,
                serverName,
                favourite,
                requestSentTimeMillis,
                friendSinceMillis,
                blockedAtMillis,
                lastSeenMillis
        );
    }

    private long toMillis(Timestamp timestamp) {
        return timestamp == null ? -1L : timestamp.getTime();
    }

    public KnownPlayerLookup knownPlayerLookup() {
        return knownPlayerLookup;
    }

    public BlocklistApplicationService blocklistService() {
        return blocklistService;
    }

    public FriendRequestApplicationService friendRequestService() {
        return friendRequestService;
    }

    public PlayerSettingsApplicationService playerSettingsService() {
        return playerSettingsService;
    }

    public FriendCommandUseCases friendCommandUseCases() {
        return friendCommandUseCases;
    }

    public ProxyActionDispatcher proxyActionDispatcher() {
        return proxyActionDispatcher;
    }
}
