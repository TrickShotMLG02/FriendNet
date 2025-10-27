package com.trickshotmlg.friendnet.core_api.interfaces.services;

import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing a player's friends and online status.
 * <p>
 * Implementations of this interface should be platform-independent, using UUIDs
 * to identify players. This interface handles friend relationships and online
 * status tracking, which can be used for cross-server communication or plugin
 * features such as joining friends' servers or displaying online status.
 * </p>
 */
public interface FriendService {

    /**
     * Adds a player to another player's friend list.
     * <p>
     * If the target player does not exist in the system yet, it should be created.
     * Implementations may optionally notify the target player that they have been added.
     * </p>
     *
     * @param player the UUID of the player who is adding a friend
     * @param target the UUID of the player to be added as a friend
     */
    void acceptFriendRequest(UUID player, UUID requester);

    void sendFriendRequest(UUID player, UUID target);

    /**
     * Removes a player from another player's friend list.
     * <p>
     * If the target player is not in the friend list, this operation should have no effect.
     * </p>
     *
     * @param player the UUID of the player who is removing a friend
     * @param target the UUID of the friend to remove
     */
    void removeFriend(UUID player, UUID target);

    /**
     * Checks whether two players are friends.
     * <p>
     * Friendship may be bidirectional or unidirectional depending on implementation.
     * Typically, adding a friend will create a mutual relationship.
     * </p>
     *
     * @param player the UUID of the first player
     * @param target the UUID of the second player
     * @return {@code true} if both players are friends, {@code false} otherwise
     */
    boolean areFriends(UUID player, UUID target);

    boolean requestPending(UUID player, UUID target);

    /**
     * Retrieves all friends of a given player.
     *
     * @param player the UUID of the player whose friends should be returned
     * @return a {@link Set} of UUIDs representing the player's friends;
     *         returns an empty set if the player has no friends
     */
    Set<UUID> getFriends(UUID player);
}
