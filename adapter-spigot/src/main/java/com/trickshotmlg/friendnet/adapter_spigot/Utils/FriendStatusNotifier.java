package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class FriendStatusNotifier {

    private FriendStatusNotifier() {
    }

    public static void notifyOnline(FriendNetPlugin plugin, UUID playerId) {
        notifyFriends(plugin, playerId, "friend.status.online");
    }

    public static void notifyOffline(FriendNetPlugin plugin, UUID playerId) {
        notifyFriends(plugin, playerId, "friend.status.offline");
    }

    private static void notifyFriends(FriendNetPlugin plugin, UUID playerId, String messageKey) {
        if (plugin == null || playerId == null) {
            return;
        }

        FriendService friendService = plugin.getFriendService();
        String displayName = SpigotUtils.getPlayerDisplayName(plugin, playerId);
        Map<String, Object> placeholders = Map.of("player", displayName);

        for (FriendshipData friendship : friendService.getFriendships(playerId)) {
            UUID friendId = friendship.getOtherPlayerId(playerId);
            Player friend = Bukkit.getPlayer(friendId);
            if (friend != null && friend.isOnline()) {
                MessageManager.send(friend, messageKey, placeholders);
            }
        }
    }
}
