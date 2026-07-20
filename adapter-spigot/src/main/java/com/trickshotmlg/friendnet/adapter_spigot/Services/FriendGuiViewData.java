package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendEntry;
import com.trickshotmlg.friendnet.core_api.proxy.payload.ProxyFriendListViewPayload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static FriendGuiViewData fromProxyPayload(UUID viewerId, ProxyFriendListViewPayload payload) {
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

    public boolean hasProxyEntry(UUID playerId) {
        return proxyEntries.containsKey(playerId);
    }

    public ProxyFriendEntry proxyEntry(UUID playerId) {
        return proxyEntries.get(playerId);
    }
}
