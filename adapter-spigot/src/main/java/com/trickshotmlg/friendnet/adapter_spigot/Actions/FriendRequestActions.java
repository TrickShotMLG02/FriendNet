package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core.application.FriendRequestApplicationService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles all friend request actions that involve interaction between players.
 * This class bridges between the platform-independent FriendService and
 * Bukkit-specific messaging and player management.
 */
public class FriendRequestActions {

    private final FriendService friendService;
    private final FriendNetPlugin plugin;
    private final FriendRequestApplicationService requestService;

    public FriendRequestActions(FriendService friendService) {
        this.friendService = friendService;
        this.plugin = null;
        this.requestService = new FriendRequestApplicationService(friendService, null);
    }

    public FriendRequestActions(FriendNetPlugin plugin) {
        this.friendService = plugin.getFriendService();
        this.plugin = plugin;
        this.requestService = plugin.getApplicationServices().friendRequestService();
    }

    /**
     * Accepts a single pending friend request sent to the given player.
     *
     * @param sender    The player accepting the request.
     * @param requester The player who originally sent the friend request.
     * @return {@code true} if the request was successfully accepted, {@code false} if no such request existed.
     */
    public boolean acceptRequest(Player sender, OfflinePlayer requester) {
        return acceptRequest(sender, requester.getUniqueId(), getDisplayName(requester));
    }

    public boolean acceptRequest(Player sender, UUID requesterId, String requesterName) {
        return switch (requestService.acceptRequest(sender.getUniqueId(), requesterId)) {
            case ACCEPTED -> {
                MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", requesterName));
                MessageManager.send(requesterId, "friendRequest.accept.target.success", Map.of("sender", sender.getName()));
                yield true;
            }
            case NOT_FOUND -> {
                MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", requesterName));
                yield false;
            }
        };
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

        if (requests.isEmpty()) {
            MessageManager.send(sender, "friendRequest.accept.sender.nonePending");
            return accepted;
        }

        for (FriendshipData r : requests) {
            UUID requesterId = r.getRequesterId();
            String targetName = getDisplayName(requesterId);

            if (requestService.acceptRequest(sender.getUniqueId(), requesterId)
                    == FriendRequestApplicationService.AcceptResult.ACCEPTED) {
                MessageManager.send(sender, "friendRequest.accept.sender.success", Map.of("target", targetName));
                MessageManager.send(requesterId, "friendRequest.accept.target.success", Map.of("sender", sender.getName()));
                accepted++;
            } else {
                MessageManager.send(sender, "friendRequest.accept.sender.notFound", Map.of("target", targetName));
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
        return denyRequest(sender, requester.getUniqueId(), getDisplayName(requester));
    }

    public boolean denyRequest(Player sender, UUID requesterId, String requesterName) {
        return switch (requestService.denyRequest(sender.getUniqueId(), requesterId)) {
            case DENIED -> {
                MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", requesterName));
                yield true;
            }
            case NOT_FOUND -> {
                MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", requesterName));
                yield false;
            }
        };
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

        if (requests.isEmpty()) {
            MessageManager.send(sender, "friendRequest.deny.sender.nonePending");
            return denied;
        }

        for (FriendshipData r : requests) {
            UUID requesterId = r.getRequesterId();
            String targetName = getDisplayName(requesterId);

            if (requestService.denyRequest(sender.getUniqueId(), requesterId)
                    == FriendRequestApplicationService.DenyResult.DENIED) {
                MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", targetName));
                denied++;
            } else {
                MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", targetName));
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
        return cancelRequest(requester, target.getUniqueId(), getDisplayName(target));
    }

    public boolean cancelRequest(Player requester, UUID targetId, String targetName) {
        return switch (requestService.cancelRequest(requester.getUniqueId(), targetId)) {
            case CANCELLED -> {
                MessageManager.send(requester, "friendRequest.cancel.sender.success", Map.of("target", targetName));
                yield true;
            }
            case NOT_FOUND -> {
                MessageManager.send(requester, "friendRequest.cancel.sender.notFound", Map.of("target", targetName));
                yield false;
            }
        };
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

        if (requests.isEmpty()) {
            MessageManager.send(sender, "friendRequest.cancel.sender.nonePending");
            return cancelled;
        }

        for (FriendshipData r : requests) {
            UUID targetId = r.getOtherPlayerId(sender.getUniqueId());
            String targetName = getDisplayName(targetId);

            if (requestService.cancelRequest(sender.getUniqueId(), targetId)
                    == FriendRequestApplicationService.CancelResult.CANCELLED) {
                MessageManager.send(sender, "friendRequest.cancel.sender.success", Map.of("target", targetName));
                cancelled++;
            } else {
                MessageManager.send(sender, "friendRequest.cancel.sender.notFound", Map.of("target", targetName));
            }
        }

        return cancelled;
    }

    private String getDisplayName(UUID playerId) {
        if (plugin != null) {
            return KnownPlayerResolver.displayName(plugin, playerId);
        }

        return playerId != null ? playerId.toString() : "Unknown";
    }

    private String getDisplayName(OfflinePlayer player) {
        UUID playerId = player.getUniqueId();
        if (plugin != null && playerId != null) {
            String displayName = SpigotUtils.getPlayerDisplayName(plugin, playerId);
            if (!displayName.isBlank()) {
                return displayName;
            }
        }

        String name = player.getName();
        if (name != null && !name.isBlank()) {
            return name;
        }

        return playerId != null ? playerId.toString() : "Unknown";
    }
}
