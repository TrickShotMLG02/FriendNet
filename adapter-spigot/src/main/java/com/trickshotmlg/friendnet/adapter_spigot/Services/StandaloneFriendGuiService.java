package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core.application.command.FriendListViewData;
import com.trickshotmlg.friendnet.core.application.proxy.ProxyActionDispatcher;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import org.bukkit.entity.Player;

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
                plugin.getApplicationServices().knownPlayerLookup(),
                this::friendListPayload
        );
    }

    @Override
    public CompletableFuture<FriendGuiViewData> friendListView(Player player) {
        FriendListViewData viewData = plugin.getApplicationServices()
                .friendCommandUseCases()
                .listViewData(player.getUniqueId());
        return CompletableFuture.completedFuture(FriendGuiViewData.local(viewData.friends(), viewData.pendingRequests()));
    }

    @Override
    public CompletableFuture<ProxyActionResponsePayload> executeAction(Player player, ProxyActionRequestPayload actionRequest) {
        return CompletableFuture.completedFuture(actionDispatcher.dispatch(player.getUniqueId(), player.getName(), actionRequest));
    }

    private ProxyFriendListViewPayload friendListPayload(UUID playerId) {
        FriendListViewData viewData = plugin.getApplicationServices()
                .friendCommandUseCases()
                .listViewData(playerId);
        return new ProxyFriendListViewPayload(
                toEntries(playerId, viewData.friends()),
                toEntries(playerId, viewData.pendingRequests())
        );
    }

    private List<ProxyFriendEntry> toEntries(UUID viewerId, List<FriendshipData> friendships) {
        return friendships.stream()
                .map(friendship -> toEntry(friendship.getOtherPlayerId(viewerId)))
                .toList();
    }

    private ProxyFriendEntry toEntry(UUID playerId) {
        return new ProxyFriendEntry(
                playerId,
                plugin.getApplicationServices().knownPlayerLookup().displayName(playerId),
                plugin.getPlayerService().isOnline(playerId),
                ""
        );
    }
}
