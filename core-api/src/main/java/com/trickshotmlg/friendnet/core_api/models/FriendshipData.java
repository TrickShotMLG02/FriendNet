package com.trickshotmlg.friendnet.core_api.models;

import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendshipData {
    private UUID player1Id;
    private UUID player2Id;
    private UUID requesterId;
    private FriendshipStatus friendshipStatus;
    private Timestamp requestSentTime;
    private Timestamp friendSince;
    private boolean isFavourite;

    /**
     * Use this constructor to create a friend request that is pending
     * @param requesterId player 1 (requesting friendship)
     * @param targetId player 2 (target of the request)
     */
    public FriendshipData(UUID requesterId, UUID targetId) {
        List<UUID> uuids = List.of(requesterId, targetId).stream().sorted().collect(Collectors.toList());
        this.player1Id = uuids.get(0);
        this.player2Id = uuids.get(1);

        this.requesterId = requesterId;
        this.friendshipStatus = FriendshipStatus.Pending;
        this.requestSentTime = new Timestamp(System.currentTimeMillis());
        this.friendSince = null;
        this.isFavourite = false;
    }

    public FriendshipData(UUID requesterId, UUID targetId, FriendshipStatus friendshipType, Timestamp requestSentTime, Timestamp friendSince, boolean isFavourite) {
        List<UUID> uuids = List.of(requesterId, targetId).stream().sorted().collect(Collectors.toList());
        this.player1Id = uuids.get(0);
        this.player2Id = uuids.get(1);

        this.requesterId = requesterId;
        this.friendshipStatus = friendshipType;
        this.requestSentTime = requestSentTime;
        this.friendSince = friendSince;
        this.isFavourite = isFavourite;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(UUID player1Id) {
        this.player1Id = player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }

    public HashSet<UUID> getPlayerIds() {
        return new HashSet<>(List.of(player1Id, player2Id));
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public Timestamp getRequestSentTime() {
        return requestSentTime;
    }

    public void setRequestSentTime(Timestamp requestSentTime) {
        this.requestSentTime = requestSentTime;
    }

    public FriendshipStatus getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    public Timestamp getFriendSince() {
        return friendSince;
    }

    public void setFriendSince(Timestamp friendSince) {
        this.friendSince = friendSince;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipData)) return false;
        FriendshipData other = (FriendshipData) o;

        // Two friendships are equal if they involve the same two players, in any order
        return (player1Id.equals(other.player1Id) && player2Id.equals(other.player2Id)) ||
                (player1Id.equals(other.player2Id) && player2Id.equals(other.player1Id));
    }

    @Override
    public int hashCode() {
        // Use a commutative combination so order doesn't matter
        // e.g., hash(a, b) = hash(b, a)
        return player1Id.hashCode() + player2Id.hashCode();
    }

    @Override
    public String toString() {
        return "FriendshipData{" +
                "player1Id=" + player1Id +
                ", player2Id=" + player2Id +
                ", requesterId=" + requesterId +
                ", friendshipType=" + friendshipStatus +
                ", requestSentTime=" + requestSentTime +
                ", friendSince=" + friendSince +
                ", isFavourite=" + isFavourite +
                '}';
    }
}
