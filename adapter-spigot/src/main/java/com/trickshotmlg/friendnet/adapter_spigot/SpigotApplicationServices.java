package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.adapter_spigot.Services.PlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.FriendStatusNotifier;
import com.trickshotmlg.friendnet.core.application.BlocklistApplicationService;
import com.trickshotmlg.friendnet.core.application.FriendRequestApplicationService;
import com.trickshotmlg.friendnet.core.application.FriendStatusVisibilityNotifier;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core.application.PlayerSettingsApplicationService;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;

public class SpigotApplicationServices {

    private final KnownPlayerLookup knownPlayerLookup;
    private final BlocklistApplicationService blocklistService;
    private final FriendRequestApplicationService friendRequestService;
    private final PlayerSettingsApplicationService playerSettingsService;
    private final FriendCommandUseCases friendCommandUseCases;

    public SpigotApplicationServices(FriendNetPlugin plugin) {
        this.knownPlayerLookup = createKnownPlayerLookup(plugin);
        this.blocklistService = createBlocklistService(plugin, knownPlayerLookup);
        this.friendRequestService = createFriendRequestService(plugin, blocklistService);
        this.playerSettingsService = createPlayerSettingsService(plugin);
        this.friendCommandUseCases = createFriendCommandUseCases(plugin);
    }

    protected KnownPlayerLookup createKnownPlayerLookup(FriendNetPlugin plugin) {
        return new KnownPlayerLookup(plugin.getPlatform(), plugin.getDatabaseService(), plugin.getPlayerService());
    }

    protected BlocklistApplicationService createBlocklistService(
            FriendNetPlugin plugin,
            KnownPlayerLookup knownPlayerLookup
    ) {
        return new BlocklistApplicationService(plugin.getDatabaseService(), plugin.getFriendService(), knownPlayerLookup);
    }

    protected FriendRequestApplicationService createFriendRequestService(
            FriendNetPlugin plugin,
            BlocklistApplicationService blocklistService
    ) {
        return new FriendRequestApplicationService(plugin.getFriendService(), blocklistService);
    }

    protected PlayerSettingsApplicationService createPlayerSettingsService(FriendNetPlugin plugin) {
        PlayerDataSaveQueue playerDataSaveQueue = plugin.getPlayerDataSaveQueue();
        FriendStatusVisibilityNotifier statusNotifier = plugin.isProxyBackendMode()
                ? FriendStatusVisibilityNotifier.NONE
                : new FriendStatusVisibilityNotifier() {
            @Override
            public void notifyOnline(java.util.UUID playerId) {
                FriendStatusNotifier.notifyOnline(plugin, playerId);
            }

            @Override
            public void notifyOffline(java.util.UUID playerId) {
                FriendStatusNotifier.notifyOffline(plugin, playerId);
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

    protected FriendCommandUseCases createFriendCommandUseCases(FriendNetPlugin plugin) {
        return new FriendCommandUseCases(
                plugin.getFriendService(),
                friendRequestService,
                blocklistService,
                knownPlayerLookup,
                plugin.getDatabaseService()
        );
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
}
