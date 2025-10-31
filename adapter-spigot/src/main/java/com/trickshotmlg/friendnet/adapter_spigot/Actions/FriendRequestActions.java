package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class FriendRequestActions {
    private final FriendService friendService;

    public FriendRequestActions(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     *
     * @param sender
     * @param requester
     * @return
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
     *
     * @param sender
     * @return
     */
    public int acceptAllRequests(Player sender) {
        Set<FriendshipData> requests = friendService.getPendingRequests(sender.getUniqueId());

        int accepted = 0;

        for(FriendshipData r : requests) {
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
     *
     * @param sender
     * @param requester
     * @return
     */
    public boolean denyRequest(Player sender, OfflinePlayer requester) {
        boolean success = friendService.denyFriendRequest(sender.getUniqueId(), requester.getUniqueId());
        if (success) {
            MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", requester.getName()));
        }
        else {
            MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", requester.getName()));
        }

        return success;
    }

    /**
     *
     * @param sender
     * @return
     */
    public int denyAllRequests(Player sender) {
        Set<FriendshipData> requests = friendService.getPendingRequests(sender.getUniqueId());

        int denied = 0;

        for(FriendshipData r : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.getRequesterId());

            boolean success = friendService.denyFriendRequest(sender.getUniqueId(), target.getUniqueId());
            if (success) {
                MessageManager.send(sender, "friendRequest.deny.sender.success", Map.of("target", target.getName()));
                denied++;
            }
            else {
                MessageManager.send(sender, "friendRequest.deny.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return denied;
    }

    /**
     *
     * @param requester
     * @param target
     * @return
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
     *
     * @param sender
     * @return
     */
    public int cancelAllRequests(Player sender) {
        Set<FriendshipData> requests = friendService.getSentRequests(sender.getUniqueId());

        int cancelled = 0;

        for(FriendshipData r : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(r.getRequesterId());

            boolean success = friendService.cancelRequest(sender.getUniqueId(), target.getUniqueId());
            if (success) {
                MessageManager.send(sender, "friendRequest.cancel.sender.success", Map.of("target", target.getName()));
                cancelled++;
            }
            else {
                MessageManager.send(sender, "friendRequest.cancel.sender.notFound", Map.of("target", target.getName()));
            }
        }

        return cancelled;
    }
}
