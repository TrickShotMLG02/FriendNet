package com.trickshotmlg.friendnet.core.application.command;

import com.trickshotmlg.friendnet.core.application.BlocklistApplicationService;
import com.trickshotmlg.friendnet.core.application.FriendRequestApplicationService;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FriendCommandUseCases {

    private final FriendService friendService;
    private final FriendRequestApplicationService requestService;
    private final BlocklistApplicationService blocklistService;
    private final KnownPlayerLookup knownPlayerLookup;
    private final DatabaseService databaseService;

    public FriendCommandUseCases(
            FriendService friendService,
            FriendRequestApplicationService requestService,
            BlocklistApplicationService blocklistService,
            KnownPlayerLookup knownPlayerLookup
    ) {
        this(friendService, requestService, blocklistService, knownPlayerLookup, null);
    }

    public FriendCommandUseCases(
            FriendService friendService,
            FriendRequestApplicationService requestService,
            BlocklistApplicationService blocklistService,
            KnownPlayerLookup knownPlayerLookup,
            DatabaseService databaseService
    ) {
        this.friendService = friendService;
        this.requestService = requestService;
        this.blocklistService = blocklistService;
        this.knownPlayerLookup = knownPlayerLookup;
        this.databaseService = databaseService;
    }

    public CommandUseCaseResult sendFriendRequest(UUID senderId, String senderName, KnownPlayerLookup.KnownPlayer target) {
        UUID targetId = target.playerId();
        String targetName = target.displayName();
        PlayerData targetData = target.playerData();

        return switch (requestService.sendFriendRequest(senderId, target)) {
            case SENT -> {
                CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true)
                        .message(CommandMessage.sender("friendRequest.send.sender.success", Map.of("target", targetName)));
                if (target.online() && targetData.isFriendRequestNotifications()) {
                    result.event(new CommandEvent(
                            CommandEventType.FRIEND_REQUEST_SENT,
                            senderId,
                            targetId,
                            Map.of("sender", senderName)
                    ));
                }
                yield result.build();
            }
            case AUTO_ACCEPTED -> {
                CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true)
                        .message(CommandMessage.sender("friendRequest.send.sender.autoAccepted", Map.of("target", targetName)));
                if (target.online() && targetData.isFriendRequestNotifications()) {
                    result.message(CommandMessage.player(
                            targetId,
                            "friendRequest.send.target.autoAccepted",
                            Map.of("sender", senderName)
                    ));
                }
                yield result.build();
            }
            case ACCEPTED_INCOMING -> {
                CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true)
                        .message(CommandMessage.sender("friendRequest.accept.sender.success", Map.of("target", targetName)));
                if (target.online()) {
                    result.message(CommandMessage.player(
                            targetId,
                            "friendRequest.accept.target.success",
                            Map.of("sender", senderName)
                    ));
                }
                yield result.build();
            }
            case CANNOT_SELF -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.send.sender.cannotSelf"))
                    .build();
            case SENDER_BLOCKED_TARGET -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("blocklist.friendRequest.senderBlocked", Map.of("target", targetName)))
                    .build();
            case TARGET_BLOCKED_SENDER -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("blocklist.friendRequest.targetBlocked", Map.of("target", targetName)))
                    .build();
            case TARGET_DISABLED_REQUESTS -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.send.sender.disabled", Map.of("target", targetName)))
                    .build();
            case AUTO_ACCEPT_FAILED -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.accept.sender.notFound", Map.of("target", targetName)))
                    .build();
            case ALREADY_PENDING -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.send.sender.alreadyPending", Map.of("target", targetName)))
                    .build();
        };
    }

    public CommandUseCaseResult acceptRequest(UUID senderId, String senderName, UUID requesterId, String requesterName) {
        return switch (requestService.acceptRequest(senderId, requesterId)) {
            case ACCEPTED -> CommandUseCaseResult.builder(true)
                    .message(CommandMessage.sender("friendRequest.accept.sender.success", Map.of("target", requesterName)))
                    .message(CommandMessage.player(requesterId, "friendRequest.accept.target.success", Map.of("sender", senderName)))
                    .build();
            case NOT_FOUND -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.accept.sender.notFound", Map.of("target", requesterName)))
                    .build();
        };
    }

    public CommandUseCaseResult acceptAllRequests(UUID senderId, String senderName) {
        Set<FriendshipData> requests = friendService.getPendingRequests(senderId);
        if (requests.isEmpty()) {
            return CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.accept.sender.nonePending"))
                    .build();
        }

        int accepted = 0;
        CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true);
        for (FriendshipData request : requests) {
            UUID requesterId = request.getRequesterId();
            String requesterName = displayName(requesterId);
            if (requestService.acceptRequest(senderId, requesterId)
                    == FriendRequestApplicationService.AcceptResult.ACCEPTED) {
                accepted++;
                result.message(CommandMessage.sender("friendRequest.accept.sender.success", Map.of("target", requesterName)));
                result.message(CommandMessage.player(requesterId, "friendRequest.accept.target.success", Map.of("sender", senderName)));
            } else {
                result.message(CommandMessage.sender("friendRequest.accept.sender.notFound", Map.of("target", requesterName)));
            }
        }

        CommandUseCaseResult builtResult = result.build();
        return new CommandUseCaseResult(accepted > 0, builtResult.messages(), builtResult.events());
    }

    public CommandUseCaseResult denyRequest(UUID senderId, UUID requesterId, String requesterName) {
        return switch (requestService.denyRequest(senderId, requesterId)) {
            case DENIED -> CommandUseCaseResult.builder(true)
                    .message(CommandMessage.sender("friendRequest.deny.sender.success", Map.of("target", requesterName)))
                    .build();
            case NOT_FOUND -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.deny.sender.notFound", Map.of("target", requesterName)))
                    .build();
        };
    }

    public CommandUseCaseResult denyAllRequests(UUID senderId) {
        Set<FriendshipData> requests = friendService.getPendingRequests(senderId);
        if (requests.isEmpty()) {
            return CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.deny.sender.nonePending"))
                    .build();
        }

        int denied = 0;
        CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true);
        for (FriendshipData request : requests) {
            UUID requesterId = request.getRequesterId();
            String requesterName = displayName(requesterId);
            if (requestService.denyRequest(senderId, requesterId)
                    == FriendRequestApplicationService.DenyResult.DENIED) {
                denied++;
                result.message(CommandMessage.sender("friendRequest.deny.sender.success", Map.of("target", requesterName)));
            } else {
                result.message(CommandMessage.sender("friendRequest.deny.sender.notFound", Map.of("target", requesterName)));
            }
        }

        CommandUseCaseResult builtResult = result.build();
        return new CommandUseCaseResult(denied > 0, builtResult.messages(), builtResult.events());
    }

    public CommandUseCaseResult cancelRequest(UUID senderId, UUID targetId, String targetName) {
        return switch (requestService.cancelRequest(senderId, targetId)) {
            case CANCELLED -> CommandUseCaseResult.builder(true)
                    .message(CommandMessage.sender("friendRequest.cancel.sender.success", Map.of("target", targetName)))
                    .build();
            case NOT_FOUND -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.cancel.sender.notFound", Map.of("target", targetName)))
                    .build();
        };
    }

    public CommandUseCaseResult cancelAllRequests(UUID senderId) {
        Set<FriendshipData> requests = friendService.getSentRequests(senderId);
        if (requests.isEmpty()) {
            return CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friendRequest.cancel.sender.nonePending"))
                    .build();
        }

        int cancelled = 0;
        CommandUseCaseResult.Builder result = CommandUseCaseResult.builder(true);
        for (FriendshipData request : requests) {
            UUID targetId = request.getOtherPlayerId(senderId);
            String targetName = displayName(targetId);
            if (requestService.cancelRequest(senderId, targetId)
                    == FriendRequestApplicationService.CancelResult.CANCELLED) {
                cancelled++;
                result.message(CommandMessage.sender("friendRequest.cancel.sender.success", Map.of("target", targetName)));
            } else {
                result.message(CommandMessage.sender("friendRequest.cancel.sender.notFound", Map.of("target", targetName)));
            }
        }

        CommandUseCaseResult builtResult = result.build();
        return new CommandUseCaseResult(cancelled > 0, builtResult.messages(), builtResult.events());
    }

    public CommandUseCaseResult removeFriend(UUID senderId, UUID targetId, String targetName) {
        if (friendService.removeFriend(senderId, targetId)) {
            return CommandUseCaseResult.builder(true)
                    .message(CommandMessage.sender("friend.remove.sender.success", Map.of("target", targetName)))
                    .build();
        }

        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("friend.remove.sender.notFound", Map.of("target", targetName)))
                .build();
    }

    public CommandUseCaseResult blockPlayer(UUID senderId, UUID targetId, String targetName) {
        return switch (blocklistService.block(senderId, targetId)) {
            case BLOCKED -> CommandUseCaseResult.builder(true)
                    .message(CommandMessage.sender("blocklist.block.success", Map.of("target", targetName)))
                    .build();
            case ALREADY_BLOCKED -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("blocklist.block.already", Map.of("target", targetName)))
                    .build();
            case SELF -> CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("blocklist.block.self"))
                    .build();
        };
    }

    public CommandUseCaseResult unblockPlayer(UUID senderId, UUID targetId, String targetName) {
        if (blocklistService.unblock(senderId, targetId)) {
            return CommandUseCaseResult.builder(true)
                    .message(CommandMessage.sender("blocklist.unblock.success", Map.of("target", targetName)))
                    .build();
        }

        return CommandUseCaseResult.builder(false)
                .message(CommandMessage.sender("blocklist.unblock.notFound", Map.of("target", targetName)))
                .build();
    }

    public CommandUseCaseResult clearBlocklist(UUID senderId) {
        int count = blocklistService.clear(senderId);
        if (count == 0) {
            return CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("blocklist.clear.empty"))
                    .build();
        }

        return CommandUseCaseResult.builder(true)
                .message(CommandMessage.sender("blocklist.clear.success", Map.of("count", count)))
                .build();
    }

    public FriendListViewData listViewData(UUID playerId) {
        return new FriendListViewData(
                friendService.getFriendships(playerId).stream().toList(),
                friendService.getPendingRequests(playerId).stream().toList()
        );
    }

    public CommandUseCaseResult setFavourite(UUID senderId, UUID targetId, String targetName, boolean favourite) {
        Optional<FriendshipData> friendship = friendService.getFriendshipData(senderId, targetId)
                .filter(data -> data.getFriendshipStatus() == FriendshipStatus.Accepted);
        if (friendship.isEmpty()) {
            return CommandUseCaseResult.builder(false)
                    .message(CommandMessage.sender("friend.remove.sender.notFound", Map.of("target", targetName)))
                    .build();
        }

        FriendshipData friendshipData = friendship.get();
        friendshipData.setFavourite(favourite);
        friendService.putFriendshipData(friendshipData);
        if (databaseService != null) {
            databaseService.save(friendshipData);
        }

        return CommandUseCaseResult.builder(true)
                .message(CommandMessage.sender(
                        favourite ? "friend.favourite.enabled" : "friend.favourite.disabled",
                        Map.of("target", targetName)
                ))
                .build();
    }

    private String displayName(UUID playerId) {
        if (knownPlayerLookup == null) {
            return playerId != null ? playerId.toString() : "Unknown";
        }

        return knownPlayerLookup.displayName(playerId);
    }
}
