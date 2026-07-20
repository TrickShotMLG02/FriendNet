package com.trickshotmlg.friendnet.core.application.command;

import com.trickshotmlg.friendnet.core_api.models.FriendshipData;

import java.util.List;

public record FriendListViewData(
        List<FriendshipData> friends,
        List<FriendshipData> pendingRequests
) {
    public FriendListViewData {
        friends = List.copyOf(friends);
        pendingRequests = List.copyOf(pendingRequests);
    }
}
