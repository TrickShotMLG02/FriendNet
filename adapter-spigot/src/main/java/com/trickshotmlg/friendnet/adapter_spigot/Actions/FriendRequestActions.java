package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core.application.FriendRequestApplicationService;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
    private final FriendCommandUseCases commandUseCases;

    public FriendRequestActions(FriendService friendService) {
        this.friendService = friendService;
        this.plugin = null;
        this.requestService = new FriendRequestApplicationService(friendService, null);
        this.commandUseCases = null;
    }

    public FriendRequestActions(FriendNetPlugin plugin) {
        this.friendService = plugin.getFriendService();
        this.plugin = plugin;
        this.requestService = plugin.getApplicationServices().friendRequestService();
        this.commandUseCases = plugin.getApplicationServices().friendCommandUseCases();
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
        if (commandUseCases == null) {
            return requestService.acceptRequest(sender.getUniqueId(), requesterId)
                    == FriendRequestApplicationService.AcceptResult.ACCEPTED;
        }

        var result = commandUseCases.acceptRequest(sender.getUniqueId(), sender.getName(), requesterId, requesterName);
        SpigotCommandResultRenderer.render(sender, result);
        return result.success();
    }

    /**
     * Accepts all pending friend requests for a given player.
     * Sends feedback messages for each processed request.
     *
     * @param sender The player whose pending requests should be accepted.
     * @return The number of requests successfully accepted.
     */
    public int acceptAllRequests(Player sender) {
        if (commandUseCases == null) {
            return requestService.acceptAllRequests(sender.getUniqueId());
        }

        int requestCount = friendService.getPendingRequests(sender.getUniqueId()).size();
        var result = commandUseCases.acceptAllRequests(sender.getUniqueId(), sender.getName());
        SpigotCommandResultRenderer.render(sender, result);
        return result.success() ? requestCount : 0;
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
        if (commandUseCases == null) {
            return requestService.denyRequest(sender.getUniqueId(), requesterId)
                    == FriendRequestApplicationService.DenyResult.DENIED;
        }

        var result = commandUseCases.denyRequest(sender.getUniqueId(), requesterId, requesterName);
        SpigotCommandResultRenderer.render(sender, result);
        return result.success();
    }

    /**
     * Denies all pending friend requests for the given player.
     * Sends feedback messages for each denied request.
     *
     * @param sender The player whose pending requests should be denied.
     * @return The number of requests successfully denied.
     */
    public int denyAllRequests(Player sender) {
        if (commandUseCases == null) {
            return requestService.denyAllRequests(sender.getUniqueId());
        }

        int requestCount = friendService.getPendingRequests(sender.getUniqueId()).size();
        var result = commandUseCases.denyAllRequests(sender.getUniqueId());
        SpigotCommandResultRenderer.render(sender, result);
        return result.success() ? requestCount : 0;
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
        if (commandUseCases == null) {
            return requestService.cancelRequest(requester.getUniqueId(), targetId)
                    == FriendRequestApplicationService.CancelResult.CANCELLED;
        }

        var result = commandUseCases.cancelRequest(requester.getUniqueId(), targetId, targetName);
        SpigotCommandResultRenderer.render(requester, result);
        return result.success();
    }

    /**
     * Cancels all friend requests previously sent by the given player.
     * Sends feedback messages for each processed request.
     *
     * @param sender The player whose outgoing friend requests should be cancelled.
     * @return The number of requests successfully cancelled.
     */
    public int cancelAllRequests(Player sender) {
        if (commandUseCases == null) {
            return requestService.cancelAllRequests(sender.getUniqueId());
        }

        int requestCount = friendService.getSentRequests(sender.getUniqueId()).size();
        var result = commandUseCases.cancelAllRequests(sender.getUniqueId());
        SpigotCommandResultRenderer.render(sender, result);
        return result.success() ? requestCount : 0;
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
