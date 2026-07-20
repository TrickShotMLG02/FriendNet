package com.trickshotmlg.friendnet.adapter_velocity.utils;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VelocityFriendStatusNotifier {

    private VelocityFriendStatusNotifier() {
    }

    public static void notifyOnline(FriendNetVelocityPlugin plugin, UUID playerId) {
        notifyFriends(plugin, playerId, "friend.status.online");
    }

    public static void notifyOffline(FriendNetVelocityPlugin plugin, UUID playerId) {
        notifyFriends(plugin, playerId, "friend.status.offline");
    }

    private static void notifyFriends(FriendNetVelocityPlugin plugin, UUID playerId, String messageKey) {
        if (plugin == null || playerId == null) {
            return;
        }

        FriendService friendService = plugin.getFriendService();
        NetworkPlayerPresence presence = plugin.getNetworkAuthorityService()
                .getPresence(playerId)
                .orElse(null);

        String displayName = presence != null && presence.displayName() != null
                ? presence.displayName()
                : plugin.getServer().getPlayer(playerId).map(player -> player.getUsername()).orElse(playerId.toString());

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("player", displayName);
        placeholders.put("server", presence != null ? presence.serverNameOptional().orElse("") : "");

        for (FriendshipData friendship : friendService.getFriendships(playerId)) {
            UUID friendId = friendship.getOtherPlayerId(playerId);
            plugin.getServer().getPlayer(friendId).ifPresent(friend ->
                    plugin.getMessageManager().send(friend, messageKey, placeholders)
            );
        }
    }
}
