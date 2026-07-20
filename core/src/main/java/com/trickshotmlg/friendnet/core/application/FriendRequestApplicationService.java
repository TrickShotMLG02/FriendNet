package com.trickshotmlg.friendnet.core.application;

import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.Set;
import java.util.UUID;

public class FriendRequestApplicationService {

    private final FriendService friendService;
    private final BlocklistApplicationService blocklistService;

    public FriendRequestApplicationService(FriendService friendService, BlocklistApplicationService blocklistService) {
        this.friendService = friendService;
        this.blocklistService = blocklistService;
    }

    public AcceptResult acceptRequest(UUID playerId, UUID requesterId) {
        return friendService.acceptFriendRequest(playerId, requesterId)
                ? AcceptResult.ACCEPTED
                : AcceptResult.NOT_FOUND;
    }

    public int acceptAllRequests(UUID playerId) {
        Set<FriendshipData> requests = friendService.getPendingRequests(playerId);
        int accepted = 0;

        for (FriendshipData request : requests) {
            if (friendService.acceptFriendRequest(playerId, request.getRequesterId())) {
                accepted++;
            }
        }

        return accepted;
    }

    public DenyResult denyRequest(UUID playerId, UUID requesterId) {
        return friendService.denyFriendRequest(playerId, requesterId)
                ? DenyResult.DENIED
                : DenyResult.NOT_FOUND;
    }

    public int denyAllRequests(UUID playerId) {
        Set<FriendshipData> requests = friendService.getPendingRequests(playerId);
        int denied = 0;

        for (FriendshipData request : requests) {
            if (friendService.denyFriendRequest(playerId, request.getRequesterId())) {
                denied++;
            }
        }

        return denied;
    }

    public CancelResult cancelRequest(UUID requesterId, UUID targetId) {
        return friendService.cancelRequest(requesterId, targetId)
                ? CancelResult.CANCELLED
                : CancelResult.NOT_FOUND;
    }

    public int cancelAllRequests(UUID requesterId) {
        Set<FriendshipData> requests = friendService.getSentRequests(requesterId);
        int cancelled = 0;

        for (FriendshipData request : requests) {
            if (friendService.cancelRequest(requesterId, request.getOtherPlayerId(requesterId))) {
                cancelled++;
            }
        }

        return cancelled;
    }

    public SendRequestResult sendFriendRequest(UUID requesterId, KnownPlayerLookup.KnownPlayer target) {
        UUID targetId = target.playerId();
        PlayerData targetData = target.playerData();

        if (targetId.equals(requesterId)) {
            return SendRequestResult.CANNOT_SELF;
        }

        if (blocklistService != null && blocklistService.isBlocked(requesterId, targetId)) {
            return SendRequestResult.SENDER_BLOCKED_TARGET;
        }

        if (blocklistService != null && blocklistService.isBlocked(targetId, requesterId)) {
            return SendRequestResult.TARGET_BLOCKED_SENDER;
        }

        if (!targetData.isAllowFriendRequests()) {
            return SendRequestResult.TARGET_DISABLED_REQUESTS;
        }

        if (friendService.sendFriendRequest(requesterId, targetId)) {
            if (targetData.isAutoAcceptFriends()) {
                return friendService.acceptFriendRequest(targetId, requesterId)
                        ? SendRequestResult.AUTO_ACCEPTED
                        : SendRequestResult.AUTO_ACCEPT_FAILED;
            }

            return SendRequestResult.SENT;
        }

        if (friendService.requestPending(requesterId, targetId)
                && friendService.acceptFriendRequest(requesterId, targetId)) {
            return SendRequestResult.ACCEPTED_INCOMING;
        }

        return SendRequestResult.ALREADY_PENDING;
    }

    public enum AcceptResult {
        ACCEPTED,
        NOT_FOUND
    }

    public enum DenyResult {
        DENIED,
        NOT_FOUND
    }

    public enum CancelResult {
        CANCELLED,
        NOT_FOUND
    }

    public enum SendRequestResult {
        SENT,
        AUTO_ACCEPTED,
        ACCEPTED_INCOMING,
        ALREADY_PENDING,
        CANNOT_SELF,
        SENDER_BLOCKED_TARGET,
        TARGET_BLOCKED_SENDER,
        TARGET_DISABLED_REQUESTS,
        AUTO_ACCEPT_FAILED
    }
}
