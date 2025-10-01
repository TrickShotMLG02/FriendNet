package com.trickshotmlg.friendnet.core_api.interfaces;

import java.util.Set;
import java.util.UUID;

public interface FriendService {
    void addFriend(UUID requester, UUID target);
    void removeFriend(UUID requester, UUID target);
    boolean areFriends(UUID a, UUID b);
    Set<UUID> getFriends(UUID requester);
    void setOnline(UUID player, boolean online);
}
