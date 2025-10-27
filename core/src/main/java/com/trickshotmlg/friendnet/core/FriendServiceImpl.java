package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendServiceImpl implements FriendService {

    private final DatabaseService databaseService;
    private final PlayerService playerService;

    public FriendServiceImpl(DatabaseService databaseService, PlayerService playerService) {
        this.databaseService = databaseService;
        this.playerService = playerService;
    }

    /**
     * Stores the friend data for all players.
     * <p>
     * The key is the player's {@link UUID}, and the value is the corresponding
     * {@link FriendData} object containing their friends list and online status.
     * <p>
     * Uses a {@link java.util.concurrent.ConcurrentHashMap} to ensure thread-safe
     * access, allowing multiple threads (e.g., event handlers, network updates)
     * to safely read and modify friend data concurrently.
     * </p>
     */
    private final Map<UUID, FriendData> friends = new ConcurrentHashMap<>();


    /**
     * Retrieves the {@link FriendData} object for the given player.
     * <p>
     * If no {@link FriendData} exists for the player, a new instance is
     * automatically created, added to the internal storage map, and returned.
     * This ensures that every player UUID queried has a corresponding
     * {@link FriendData} object.
     * </p>
     *
     * @param player the UUID of the player whose {@link FriendData} is requested
     * @return the existing or newly created {@link FriendData} associated with the player
     */
    private FriendData getOrCreate(UUID player) {
        return friends.computeIfAbsent(player, FriendData::new);
    }

    /**
     * @param player    the UUID of the player who is adding a friend
     * @param requester
     */
    @Override
    public void acceptFriendRequest(UUID player, UUID requester) {

    }

    /**
     * @param player
     * @param target
     */
    @Override
    public void sendFriendRequest(UUID player, UUID target) {

    }

    @Override
    public void removeFriend(UUID player, UUID target) {
        getOrCreate(player).removeFriend(target);
        getOrCreate(target).removeFriend(player);
    }

    @Override
    public boolean areFriends(UUID player, UUID target) {
        return getOrCreate(player).getFriends().contains(target);
    }

    /**
     * @param player
     * @param target
     * @return
     */
    @Override
    public boolean requestPending(UUID player, UUID target) {
        return false;
    }

    @Override
    public Set<UUID> getFriends(UUID player) {
        return getOrCreate(player).getFriends();
    }
}
