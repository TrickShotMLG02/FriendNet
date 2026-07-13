package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

/**
 * Handles all friend request actions that involve interaction between players.
 * This class bridges between the platform-independent FriendService and
 * Bukkit-specific messaging and player management.
 */
public class FriendRequestActions {

    private final FriendService friendService;

    public FriendRequestActions(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * Accepts a single pending friend request sent to the given player.
     *
     * @param sender    The player accepting the request.
     * @param requester The player who originally sent the friend request.
     * @return {@code true} if the request was successfully accepted, {@code false} if no such request existed.
     */
    public boolean acceptRequest(Player sender, OfflinePlayer requester) {
        boolean success = friendService.acceptFriendRequest(sender.getUniqueId(), requester.getUniqueId());

        if (success) {
            MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", requester.getName()));
            MessageManager.send(requester, "friendRequest.accept.target.success", Map.of("sender", sender.getName()));
        } else {
            MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", requester.getName()));
        }

        return success;
    }

    /**
     * Accepts all pending friend requests for a given player.
     * Sends feedback messages for each processed request.
     *
     * @param sender The player whose pending requests should be accepted.
     * @return The number of requests successfully accepted.
     */
    public int acceptAllRequests(Player sender) {
        Set<FriendshipData> requests = friendService.getPendingRequests(sender.getUniqueId());
        int accepted = 0;

        // TODO: send message like No pending requests

        for (FriendshipData r : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.getRequesterId());

            boolean success = friendService.acceptFriendRequest(sender.getUniqueId(), target.getUniqueId());

            if (success) {
                MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", target.getName()));
                MessageManager.send(target, "friendRequest.accept.target.success", Map.of("sender", sender.getName()));
                accepted++;
            } else {
                MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return accepted;
    }

    /**
     * Denies a specific pending friend request sent to the given player.
     *
     * @param sender    The player denying the request.
     * @param requester The player who sent the friend request.
     * @return {@code true} if the request was successfully denied, {@code false} if no such request existed.
     */
    public boolean denyRequest(Player sender, OfflinePlayer requester) {
        boolean success = friendService.denyFriendRequest(sender.getUniqueId(), requester.getUniqueId());

        if (success) {
            MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", requester.getName()));
        } else {
            MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", requester.getName()));
        }

        return success;
    }

    /**
     * Denies all pending friend requests for the given player.
     * Sends feedback messages for each denied request.
     *
     * @param sender The player whose pending requests should be denied.
     * @return The number of requests successfully denied.
     */
    public int denyAllRequests(Player sender) {
        Set<FriendshipData> requests = friendService.getPendingRequests(sender.getUniqueId());
        int denied = 0;

        // TODO: send message like No pending requests

        for (FriendshipData r : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.getRequesterId());

            boolean success = friendService.denyFriendRequest(sender.getUniqueId(), target.getUniqueId());
            if (success) {
                MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", target.getName()));
                denied++;
            } else {
                MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return denied;
    }

    /**
     * Cancels a previously sent friend request.
     * This is used when a player retracts a pending request before it is accepted or denied.
     *
     * @param requester The player cancelling the request.
     * @param target    The player who received the request.
     * @return {@code true} if the request was successfully cancelled, {@code false} if it no longer existed.
     */
    public boolean cancelRequest(Player requester, OfflinePlayer target) {
        boolean success = friendService.cancelRequest(requester.getUniqueId(), target.getUniqueId());

        if (success) {
            MessageManager.send(requester, "friendRequest.cancel.sender.success", Map.of("target", target.getName()));
        } else {
            MessageManager.send(requester, "friendRequest.cancel.sender.notFound", Map.of("target", target.getName()));
        }

        return success;
    }

    /**
     * Cancels all friend requests previously sent by the given player.
     * Sends feedback messages for each processed request.
     *
     * @param sender The player whose outgoing friend requests should be cancelled.
     * @return The number of requests successfully cancelled.
     */
    public int cancelAllRequests(Player sender) {
        Set<FriendshipData> requests = friendService.getSentRequests(sender.getUniqueId());
        int cancelled = 0;

        for (FriendshipData r : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.getOtherPlayerId(sender.getUniqueId()));

            boolean success = friendService.cancelRequest(sender.getUniqueId(), target.getUniqueId());
            if (success) {
                MessageManager.send(sender, "friendRequest.cancel.sender.success", Map.of("target", target.getName()));
                cancelled++;
            } else {
                MessageManager.send(sender, "friendRequest.cancel.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return cancelled;
    }
}
