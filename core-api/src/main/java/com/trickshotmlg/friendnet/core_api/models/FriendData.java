package com.trickshotmlg.friendnet.core_api.models;

import com.trickshotmlg.friendnet.core_api.enums.FriendshipType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents the friend data of a single player.
 * <p>
 * This class is platform-independent and stores the player's friends list
 * and online status. Each player is identified by their unique UUID.
 * </p>
 */
public class FriendData {

    /**
     * The UUID of the player this data belongs to.
     */
    private final UUID playerId;

    private final Set<FriendshipData> friends = Set.of();

    /**
     * Creates a new {@link FriendData} instance for the specified player.
     *
     * @param playerId the UUID of the player
     */
    public FriendData(UUID playerId) {
        this.playerId = playerId;
    }

    /**
     * Returns the UUID of the player.
     *
     * @return the player's UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    public Set<UUID> getFriends() {
        Set<UUID> p1 = friends.stream().map(fd -> fd.getPlayer1Id()).collect(Collectors.toSet());
        p1.addAll(friends.stream().map(fd -> fd.getPlayer2Id()).collect(Collectors.toSet()));
        return p1;
    }

    /**
     * Adds a friend to the player's friends list.
     * <p>
     * If the friend is already in the list, this method has no effect.
     * </p>
     *
     * @param friendId the UUID of the friend to add
     */
    public void addFriend(UUID friendId, FriendshipType type) {
        //friends.computeIfAbsent(friendId, k -> Set.of());
        //friends.get(friendId)
        //friends.add(friendId);
    }

    /**
     * Removes a friend from the player's friends list.
     * <p>
     * If the friend is not in the list, this method has no effect.
     * </p>
     *
     * @param friendId the UUID of the friend to remove
     */
    public void removeFriend(UUID friendId) {
        friends.remove(friendId);
    }
}
