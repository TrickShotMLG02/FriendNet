package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionRequestPayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyActionResponsePayload;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyBackendFriendGuiService implements FriendGuiService {

    private final FriendNetPlugin plugin;

    public ProxyBackendFriendGuiService(FriendNetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<FriendGuiViewData> friendListView(Player player) {
        return plugin.getProxyMessagingClient()
                .requestFriendListView(player)
                .thenApply(payload -> toViewData(player.getUniqueId(), payload));
    }

    @Override
    public CompletableFuture<ProxyActionResponsePayload> executeAction(Player player, ProxyActionRequestPayload actionRequest) {
        return plugin.getProxyMessagingClient().executeFriendAction(player, actionRequest);
    }

    private FriendGuiViewData toViewData(UUID viewerId, ProxyFriendListViewPayload payload) {
        List<FriendshipData> friends = payload.friends().stream()
                .map(entry -> new FriendshipData(
                        viewerId,
                        entry.playerId(),
                        FriendshipStatus.Accepted,
                        null,
                        null,
                        false
                ))
                .toList();
        List<FriendshipData> pendingRequests = payload.pendingRequests().stream()
                .map(entry -> new FriendshipData(
                        entry.playerId(),
                        viewerId,
                        FriendshipStatus.Pending,
                        null,
                        null,
                        false
                ))
                .toList();
        Map<UUID, ProxyFriendEntry> entries = Stream.concat(payload.friends().stream(), payload.pendingRequests().stream())
                .collect(Collectors.toMap(ProxyFriendEntry::playerId, entry -> entry, (left, right) -> left));
        return new FriendGuiViewData(friends, pendingRequests, entries);
    }
}
