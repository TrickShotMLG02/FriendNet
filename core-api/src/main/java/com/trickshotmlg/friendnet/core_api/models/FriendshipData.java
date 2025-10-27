package com.trickshotmlg.friendnet.core_api.models;

import com.trickshotmlg.friendnet.core_api.enums.FriendshipType;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendshipData {
    private UUID player1Id;
    private UUID player2Id;
    private UUID requesterId;
    private FriendshipType friendshipType;
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
        this.friendshipType = FriendshipType.Pending;
        this.requestSentTime = new Timestamp(System.currentTimeMillis());
        this.friendSince = null;
        this.isFavourite = false;
    }

    public FriendshipData(UUID requesterId, UUID targetId, FriendshipType friendshipType, Timestamp requestSentTime, Timestamp friendSince, boolean isFavourite) {
        List<UUID> uuids = List.of(requesterId, targetId).stream().sorted().collect(Collectors.toList());
        this.player1Id = uuids.get(0);
        this.player2Id = uuids.get(1);

        this.requesterId = requesterId;
        this.friendshipType = friendshipType;
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

    public FriendshipType getFriendshipType() {
        return friendshipType;
    }

    public void setFriendshipType(FriendshipType friendshipType) {
        this.friendshipType = friendshipType;
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
    public String toString() {
        return "FriendshipData{" +
                "player1Id=" + player1Id +
                ", player2Id=" + player2Id +
                ", requesterId=" + requesterId +
                ", friendshipType=" + friendshipType +
                ", requestSentTime=" + requestSentTime +
                ", friendSince=" + friendSince +
                ", isFavourite=" + isFavourite +
                '}';
    }
}
