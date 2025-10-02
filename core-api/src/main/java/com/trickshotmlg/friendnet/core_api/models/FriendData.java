package com.trickshotmlg.friendnet.core_api.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    /**
     * The set of UUIDs representing the player's friends.
     * <p>
     * Duplicate entries are not allowed. Modifications should be done via
     * {@link #addFriend(UUID)} and {@link #removeFriend(UUID)}.
     * </p>
     */
    private final Set<UUID> friends = new HashSet<>();

    /**
     * Whether the player is currently online.
     */
    private boolean online;

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

    /**
     * Returns the set of friends of this player.
     * <p>
     * Note: Modifying the returned set directly is allowed but not
     * recommended. Prefer using {@link #addFriend(UUID)} and
     * {@link #removeFriend(UUID)} for consistency.
     * </p>
     *
     * @return a {@link Set} of UUIDs representing the player's friends
     */
    public Set<UUID> getFriends() {
        return friends;
    }

    /**
     * Returns whether the player is currently online.
     *
     * @return {@code true} if the player is online, {@code false} otherwise
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Sets the player's online status.
     *
     * @param online {@code true} to mark the player as online, {@code false} to mark offline
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Adds a friend to the player's friends list.
     * <p>
     * If the friend is already in the list, this method has no effect.
     * </p>
     *
     * @param friendId the UUID of the friend to add
     */
    public void addFriend(UUID friendId) {
        friends.add(friendId);
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
