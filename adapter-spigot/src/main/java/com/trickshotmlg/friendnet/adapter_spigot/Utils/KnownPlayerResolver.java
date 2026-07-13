package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
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

        Player onlineTarget = Bukkit.getPlayerExact(name);
        if (onlineTarget != null) {
            PlayerData playerData = plugin.getPlayerService().getPlayerData(onlineTarget.getUniqueId());
            if (playerData == null) {
                playerData = plugin.getDatabaseService()
                        .find(onlineTarget.getUniqueId(), PlayerData.class)
                        .orElseGet(() -> plugin.getPlayerService().initPlayer(onlineTarget.getUniqueId()));
                playerData.setLastDisplayName(onlineTarget.getDisplayName());
                plugin.getPlayerService().putPlayerData(playerData);
            }

            return Optional.of(new KnownPlayerTarget(onlineTarget, playerData, onlineTarget.getName()));
        }

        return plugin.getDatabaseService().findPlayerByLastDisplayName(name)
                .map(playerData -> {
                    plugin.getPlayerService().putPlayerData(playerData);
                    return new KnownPlayerTarget(null, playerData, displayNameOrFallback(playerData));
                });
    }

    public static String displayName(FriendNetPlugin plugin, UUID playerId) {
        if (plugin != null && playerId != null) {
            String displayName = SpigotUtils.getPlayerDisplayName(plugin, playerId);
            if (displayName != null && !displayName.isBlank()) {
                return displayName;
            }
        }

        return playerId != null ? playerId.toString() : "Unknown";
    }

    public static List<String> suggestFriendshipPlayers(
            FriendNetPlugin plugin,
            Collection<FriendshipData> friendships,
            UUID viewerId,
            String prefix
    ) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase();
        return friendships.stream()
                .map(friendship -> displayName(plugin, friendship.getOtherPlayerId(viewerId)))
                .filter(name -> name.toLowerCase().startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
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

    private static String displayNameOrFallback(PlayerData playerData) {
        String displayName = playerData.getLastDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }

        return playerData.getPlayerId().toString();
    }

    public record KnownPlayerTarget(Player onlinePlayer, PlayerData playerData, String displayName) {
        public UUID playerId() {
            return playerData.getPlayerId();
        }
    }
}
