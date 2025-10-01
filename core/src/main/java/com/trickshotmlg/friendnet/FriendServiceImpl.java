package com.trickshotmlg.friendnet;

import com.trickshotmlg.friendnet.core_api.interfaces.FriendService;

import java.util.*;

public class FriendServiceImpl implements FriendService {

    private final Map<UUID, Set<UUID>> friendMap = new HashMap<>();
    private final Set<UUID> onlinePlayers = new HashSet<>();

    /**
     * @param requester
     * @param target
     */
    @Override
    public void addFriend(UUID requester, UUID target) {
        friendMap.computeIfAbsent(requester, k -> new HashSet<>()).add(target);
        friendMap.computeIfAbsent(target, k -> new HashSet<>()).add(requester);
    }

    /**
     * @param requester
     * @param target
     */
    @Override
    public void removeFriend(UUID requester, UUID target) {
        friendMap.getOrDefault(requester, Set.of()).remove(target);
        friendMap.getOrDefault(target, Set.of()).remove(requester);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    @Override
    public boolean areFriends(UUID a, UUID b) {
        return friendMap.getOrDefault(a, Set.of()).contains(b);
    }

    /**
     * @param requester
     * @return
     */
    @Override
    public Set<UUID> getFriends(UUID requester) {
        return Collections.unmodifiableSet(friendMap.getOrDefault(requester, Set.of()));
    }

    /**
     * @param player
     * @param online
     */
    @Override
    public void setOnline(UUID player, boolean online) {
        if (online) {
            onlinePlayers.add(player);
        } else {
            onlinePlayers.remove(player);
        }
    }
}
