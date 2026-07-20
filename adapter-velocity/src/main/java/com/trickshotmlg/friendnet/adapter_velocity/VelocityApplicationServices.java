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
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;

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
                VelocityFriendStatusNotifier.notifyOnline(plugin, playerId);
            }

            @Override
            public void notifyOffline(java.util.UUID playerId) {
                VelocityFriendStatusNotifier.notifyOffline(plugin, playerId);
            }
        };

        return new PlayerSettingsApplicationService(
                plugin.getPlayerService(),
                playerId -> {
                    if (playerDataSaveQueue != null) {
                        playerDataSaveQueue.markDirty(playerId);
                    }
                },
                statusNotifier
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
                knownPlayerLookup,
                this::friendListViewPayload
        );
    }

    public ProxyFriendListViewPayload friendListViewPayload(UUID viewerId) {
        FriendListViewData viewData = friendCommandUseCases.listViewData(viewerId);
        return new ProxyFriendListViewPayload(
                toFriendEntries(viewerId, viewData.friends()),
                toFriendEntries(viewerId, viewData.pendingRequests()),
                toFriendEntries(viewerId, plugin.getFriendService().getSentRequests(viewerId).stream().toList()),
                blocklistService.getBlockedPlayers(viewerId).stream()
                        .map(blockedPlayer -> toEntry(blockedPlayer.getBlockedId(), false))
                        .toList()
        );
    }

    private List<ProxyFriendEntry> toFriendEntries(UUID viewerId, List<FriendshipData> friendships) {
        return friendships.stream()
                .map(friendship -> toEntry(friendship.getOtherPlayerId(viewerId), friendship.isFavourite()))
                .toList();
    }

    private ProxyFriendEntry toEntry(UUID playerId, boolean favourite) {
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

        return new ProxyFriendEntry(playerId, displayName, online, serverName, favourite);
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
