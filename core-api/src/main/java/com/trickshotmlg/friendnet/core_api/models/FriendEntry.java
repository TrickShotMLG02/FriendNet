package com.trickshotmlg.friendnet.core_api.models;

import java.sql.Timestamp;
import java.util.UUID;

public record FriendEntry(
        FriendshipData friendship,
        UUID friendId,
        boolean favourite
) {
    public FriendEntry {
        if (friendship == null) {
            throw new IllegalArgumentException("Friendship cannot be null.");
        }
        if (friendId == null) {
            throw new IllegalArgumentException("Friend id cannot be null.");
        }
    }

    public Timestamp getRequestSentTime() {
        return friendship.getRequestSentTime();
    }

    public Timestamp getFriendSince() {
        return friendship.getFriendSince();
    }
}
