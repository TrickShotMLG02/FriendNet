package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core.application.KnownPlayerLookup;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class KnownPlayerResolver {

    private KnownPlayerResolver() {
    }

    public static Optional<KnownPlayerTarget> resolve(FriendNetPlugin plugin, String name) {
        if (plugin == null || name == null || name.isBlank()) {
            return Optional.empty();
        }

        return createLookup(plugin).resolve(name)
                .map(player -> new KnownPlayerTarget(
                        Bukkit.getPlayer(player.playerId()),
                        player.playerData(),
                        player.displayName()
                ));
    }

    public static String displayName(FriendNetPlugin plugin, UUID playerId) {
        if (plugin == null) {
            return playerId != null ? playerId.toString() : "Unknown";
        }

        return createLookup(plugin).displayName(playerId);
    }

    public static List<String> suggestFriendshipPlayers(
            FriendNetPlugin plugin,
            Collection<FriendshipData> friendships,
            UUID viewerId,
            String prefix
    ) {
        return createLookup(plugin).suggestFriendshipPlayerNames(friendships, viewerId, prefix);
    }

    public static List<String> suggestOnlinePlayers(Player sender, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> sender == null || !player.getUniqueId().equals(sender.getUniqueId()))
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static KnownPlayerLookup createLookup(FriendNetPlugin plugin) {
        return plugin.getApplicationServices().knownPlayerLookup();
    }

    public record KnownPlayerTarget(Player onlinePlayer, PlayerData playerData, String displayName) {
        public UUID playerId() {
            return playerData.getPlayerId();
        }
    }
}
