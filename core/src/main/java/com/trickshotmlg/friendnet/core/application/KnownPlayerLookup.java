package com.trickshotmlg.friendnet.core.application;

import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KnownPlayerLookup {

    private final Platform platform;
    private final DatabaseService databaseService;
    private final PlayerService playerService;

    public KnownPlayerLookup(Platform platform, DatabaseService databaseService, PlayerService playerService) {
        this.platform = platform;
        this.databaseService = databaseService;
        this.playerService = playerService;
    }

    public Optional<KnownPlayer> resolve(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        Optional<PlatformPlayer> onlinePlayer = platform.getOnlinePlayers().stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .findFirst();

        if (onlinePlayer.isPresent()) {
            PlatformPlayer player = onlinePlayer.get();
            PlayerData playerData = ensurePlayerData(player);
            return Optional.of(new KnownPlayer(player.getUniqueId(), displayName(player), playerData, true));
        }

        return databaseService.findPlayerByLastDisplayName(name)
                .map(playerData -> {
                    playerService.putPlayerData(playerData);
                    return new KnownPlayer(playerData.getPlayerId(), displayNameOrFallback(playerData), playerData, false);
                });
    }

    public String displayName(UUID playerId) {
        if (playerId == null) {
            return "Unknown";
        }

        PlatformPlayer onlinePlayer = platform.getPlayer(playerId);
        if (onlinePlayer != null) {
            return displayName(onlinePlayer);
        }

        PlayerData cachedData = playerService.getPlayerData(playerId);
        if (cachedData != null) {
            return displayNameOrFallback(cachedData);
        }

        return databaseService.find(playerId, PlayerData.class)
                .map(playerData -> {
                    playerService.putPlayerData(playerData);
                    return displayNameOrFallback(playerData);
                })
                .orElse(playerId.toString());
    }

    public List<String> suggestFriendshipPlayers(Collection<FriendshipData> friendships, UUID viewerId, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase();
        return friendships.stream()
                .map(friendship -> displayName(friendship.getOtherPlayerId(viewerId)))
                .filter(name -> name.toLowerCase().startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> suggestOnlinePlayers(UUID senderId, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase();
        return platform.getOnlinePlayers().stream()
                .filter(player -> senderId == null || !player.getUniqueId().equals(senderId))
                .map(PlatformPlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private PlayerData ensurePlayerData(PlatformPlayer player) {
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        if (playerData == null) {
            playerData = databaseService.find(player.getUniqueId(), PlayerData.class)
                    .orElseGet(() -> playerService.initPlayer(player.getUniqueId()));
        }

        playerData.setLastDisplayName(displayName(player));
        playerService.putPlayerData(playerData);
        return playerData;
    }

    private String displayName(PlatformPlayer player) {
        String displayName = player.getDisplayName();
        return displayName == null || displayName.isBlank() ? player.getName() : displayName;
    }

    private String displayNameOrFallback(PlayerData playerData) {
        String displayName = playerData.getLastDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }

        return playerData.getPlayerId().toString();
    }

    public record KnownPlayer(UUID playerId, String displayName, PlayerData playerData, boolean online) {
    }
}
