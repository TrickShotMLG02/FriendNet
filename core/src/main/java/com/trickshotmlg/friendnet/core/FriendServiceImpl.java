package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class FriendServiceImpl implements FriendService {

    private final DatabaseService databaseService;
    private final PlayerService playerService;

    public FriendServiceImpl(DatabaseService databaseService, PlayerService playerService) {
        this.databaseService = databaseService;
        this.playerService = playerService;
    }

    private final HashSet<FriendshipData> friends = new HashSet<>();

    @Override
    public boolean acceptFriendRequest(UUID player, UUID requester) {
        // TODO: Verify that only the target player can accept request from requester
        Optional<FriendshipData> request = getFriendshipData(player, requester);
        if (request.isPresent() && request.get().getFriendshipStatus() == FriendshipStatus.Pending) {
            Instant now = Instant.now();
            ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));
            Timestamp friendSince = Timestamp.from(zdt.toInstant());

            request.get().setFriendshipStatus(FriendshipStatus.Accepted);
            request.get().setFriendSince(friendSince);
            databaseService.save(request.get());
            return true;
        }
        return false;
    }

    /**
     * @param player    The UUID of the player denying the friend request.
     * @param requester The UUID of the player who sent the friend request.
     * @return
     */
    @Override
    public boolean denyFriendRequest(UUID player, UUID requester) {
        Optional<FriendshipData> request = getFriendshipData(player, requester);
        if (request.isPresent() && request.get().getFriendshipStatus() == FriendshipStatus.Pending) {
            friends.remove(request.get());
            databaseService.delete(request.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean sendFriendRequest(UUID player, UUID target) {
        FriendshipData request = new FriendshipData(player, target);

        if (friends.contains(request)) {
            return false;
        }
        else {
            friends.add(request);
            databaseService.save(request);
            return true;
        }
    }

    @Override
    public boolean removeFriend(UUID player, UUID target) {
        Optional<FriendshipData> friendship = getFriendshipData(player, target);
        if (friendship.isPresent() && friendship.get().getFriendshipStatus() == FriendshipStatus.Accepted) {
            removeFriendshipData(friendship.get());
            databaseService.delete(friendship.get());

            return true;
        }
        return false;
    }

    /**
     * @param requester
     * @param target
     * @return
     */
    @Override
    public boolean cancelRequest(UUID requester, UUID target) {
        Optional<FriendshipData> friendship = getFriendshipData(requester, target);
        if (friendship.isPresent() && friendship.get().getFriendshipStatus() == FriendshipStatus.Pending && friendship.get().getRequesterId().equals(requester)) {
            removeFriendshipData(friendship.get());
            databaseService.delete(friendship.get());

            return true;
        }
        return false;
    }

    @Override
    public boolean areFriends(UUID player, UUID target) {
        HashSet<UUID> targets = new HashSet<>(List.of(player, target));
        return friends.stream().anyMatch(f -> f.getPlayerIds().equals(targets) && f.getFriendshipStatus() == FriendshipStatus.Accepted);
    }

    @Override
    public boolean requestPending(UUID player, UUID target) {
        Optional<FriendshipData> friendship = getFriendshipData(player, target);

        if (friendship.isPresent()) {
            return friendship.get().getRequesterId().equals(player);
        }

        return false;
    }

    @Override
    public Set<FriendshipData> getFriendships(UUID player) {
        List<FriendshipData> friendships = friends.stream()
            .filter(f ->
                    f.getPlayerIds().contains(player) &&
                    f.getFriendshipStatus() == FriendshipStatus.Accepted
            )
        .toList();

        return new HashSet<>(friendships);
    }

    @Override
    public Set<FriendshipData> getPendingRequests(UUID player) {
        List<FriendshipData> requests = friends.stream()
            .filter(f ->
                f.getPlayerIds().contains(player) &&
                !f.getRequesterId().equals(player) &&
                f.getFriendshipStatus() == FriendshipStatus.Pending
            )
        .toList();

        return new HashSet<>(requests);
    }

    @Override
    public Set<FriendshipData> getSentRequests(UUID player) {
        List<FriendshipData> requests = friends.stream()
            .filter(f ->
                    f.getRequesterId().equals(player) &&
                    f.getFriendshipStatus() == FriendshipStatus.Pending
            )
        .toList();

        return new HashSet<>(requests);
    }

    @Override
    public Optional<FriendshipData> getFriendshipData(UUID player1, UUID player2) {
        HashSet<UUID> targets = new HashSet<>(List.of(player1, player2));
        return friends.stream().filter(f -> f.getPlayerIds().equals(targets)).findFirst();
    }

    @Override
    public boolean putFriendshipData(FriendshipData friendshipData) {
        boolean success = friends.add(friendshipData);
        if(!success) {
            // force update wih new friendship data
            friends.remove(friendshipData);
            friends.add(friendshipData);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeFriendshipData(FriendshipData friendshipData) {
        return friends.remove(friendshipData);
    }
}
