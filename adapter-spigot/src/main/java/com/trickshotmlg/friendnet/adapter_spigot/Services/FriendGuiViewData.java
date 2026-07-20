package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FriendGuiViewData(
        List<FriendshipData> friends,
        List<FriendshipData> pendingRequests,
        Map<UUID, ProxyFriendEntry> proxyEntries
) {
    public FriendGuiViewData {
        friends = friends == null ? List.of() : List.copyOf(friends);
        pendingRequests = pendingRequests == null ? List.of() : List.copyOf(pendingRequests);
        proxyEntries = proxyEntries == null ? Map.of() : Map.copyOf(proxyEntries);
    }

    public static FriendGuiViewData local(List<FriendshipData> friends, List<FriendshipData> pendingRequests) {
        return new FriendGuiViewData(friends, pendingRequests, Map.of());
    }

    public boolean hasProxyEntry(UUID playerId) {
        return proxyEntries.containsKey(playerId);
    }

    public ProxyFriendEntry proxyEntry(UUID playerId) {
        return proxyEntries.get(playerId);
    }
}
