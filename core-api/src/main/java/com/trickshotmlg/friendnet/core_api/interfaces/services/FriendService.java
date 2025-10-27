package com.trickshotmlg.friendnet.core_api.interfaces.services;

import com.trickshotmlg.friendnet.core_api.models.FriendshipData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Provides operations for managing friendships between players in a platform-independent way.
 * <p>
 * This service handles sending friend requests, accepting requests, removing friends,
 * and querying friendship status or pending requests. All player identities are represented by {@link UUID}.
 * Implementations should ensure thread-safe access and consistent state management.
 */
public interface FriendService {

    /**
     * Accepts a pending friend request from a requester to the specified player.
     * <p>
     * After this operation, both players should be considered friends.
     *
     * @param player    The UUID of the player accepting the friend request.
     * @param requester The UUID of the player who sent the friend request.
     * @return
     * @throws IllegalArgumentException if there is no pending request from {@code requester} to {@code player}.
     */
    boolean acceptFriendRequest(UUID player, UUID requester);

    /**
     * Sends a friend request from one player to another.
     * <p>
     * If a request already exists in either direction, this method may either
     * update the request timestamp or do nothing depending on implementation.
     *
     * @param player The UUID of the player sending the request.
     * @param target The UUID of the target player to whom the request is sent.
     * @return True if request was sent successfully, else False
     * @throws IllegalArgumentException if {@code player} equals {@code target}.
     */
    boolean sendFriendRequest(UUID player, UUID target);

    /**
     * Removes an existing friendship between two players.
     * <p>
     * If the players are not friends, this method may do nothing or throw an exception
     * depending on implementation.
     *
     * @param player The UUID of the first player.
     * @param target The UUID of the second player.
     */
    boolean removeFriend(UUID player, UUID target);

    /**
     *
     * @param requester
     * @param target
     * @return
     */
    boolean cancelRequest(UUID requester, UUID target);

    /**
     * Checks whether two players are currently friends.
     *
     * @param player The UUID of the first player.
     * @param target The UUID of the second player.
     * @return {@code true} if the players are friends; {@code false} otherwise.
     */
    boolean areFriends(UUID player, UUID target);

    /**
     * Checks whether there is a pending friend request between two players.
     *
     * @param player The UUID of the potential recipient of the request.
     * @param target The UUID of the potential sender of the request.
     * @return {@code true} if there is a pending request from {@code target} to {@code player};
     *         {@code false} otherwise.
     */
    boolean requestPending(UUID player, UUID target);

    /**
     * Retrieves all confirmed friendships of a player.
     *
     * @param player The UUID of the player whose friendships are requested.
     * @return A set of {@link FriendshipData} representing all friendships of the player.
     *         The set may be empty if the player has no friends.
     */
    Set<FriendshipData> getFriendships(UUID player);

    /**
     * Retrieves all incoming friend requests that are pending for the specified player.
     *
     * @param player The UUID of the player whose incoming requests are requested.
     * @return A set of {@link FriendshipData} representing all pending friend requests
     *         received by the player.
     */
    Set<FriendshipData> getPendingRequests(UUID player);

    /**
     * Retrieves all outgoing friend requests that have been sent by the specified player
     * and are still pending.
     *
     * @param player The UUID of the player whose sent requests are requested.
     * @return A set of {@link FriendshipData} representing all pending requests sent by the player.
     */
    Set<FriendshipData> getSentRequests(UUID player);

    /**
     * Retrieves the friendship data between two players, if it exists.
     * <p>
     * This includes both confirmed friendships and pending friend requests.
     *
     * @param player1 The UUID of the first player.
     * @param player2 The UUID of the second player.
     * @return An {@link Optional} containing the {@link FriendshipData} if a friendship
     *         or pending request exists; otherwise, {@link Optional#empty()}.
     */
    Optional<FriendshipData> getFriendshipData(UUID player1, UUID player2);

    boolean putFriendshipData(FriendshipData friendshipData);

    boolean removeFriendshipData(FriendshipData friendshipData);
}
