package com.trickshotmlg.friendnet.core.application;

import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.PlatformPlayer;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KnownPlayerLookup {

    private final Platform platform;
    private final DatabaseService databaseService;
    private final PlayerService playerService;
    private final Map<String, UUID> playerIdsByLowerName = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> knownPlayersById = new ConcurrentHashMap<>();

    public KnownPlayerLookup(Platform platform, DatabaseService databaseService, PlayerService playerService) {
        this.platform = platform;
        this.databaseService = databaseService;
        this.playerService = playerService;
    }

    public void loadKnownPlayers() {
        for (PlayerData playerData : databaseService.findAllPlayerData()) {
            remember(playerData);
        }
    }

    public void remember(PlayerData playerData) {
        if (playerData == null || playerData.getPlayerId() == null) {
            return;
        }

        UUID playerId = playerData.getPlayerId();
        playerIdsByLowerName.entrySet().removeIf(entry -> entry.getValue().equals(playerId));
        knownPlayersById.put(playerData.getPlayerId(), playerData);
        String playerName = playerData.getLastPlayerName();
        if (playerName != null && !playerName.isBlank()) {
            playerIdsByLowerName.put(playerName.toLowerCase(Locale.ROOT), playerId);
        }
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

        PlayerData cachedPlayer = knownPlayerByName(name);
        if (cachedPlayer != null) {
            playerService.putPlayerData(cachedPlayer);
            return Optional.of(new KnownPlayer(cachedPlayer.getPlayerId(), displayNameOrFallback(cachedPlayer), cachedPlayer, false));
        }

        return databaseService.findPlayerByLastPlayerName(name)
                .map(playerData -> {
                    playerService.putPlayerData(playerData);
                    remember(playerData);
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
            remember(cachedData);
            return displayNameOrFallback(cachedData);
        }

        PlayerData knownData = knownPlayersById.get(playerId);
        if (knownData != null) {
            playerService.putPlayerData(knownData);
            return displayNameOrFallback(knownData);
        }

        return databaseService.find(playerId, PlayerData.class)
                .map(playerData -> {
                    playerService.putPlayerData(playerData);
                    remember(playerData);
                    return displayNameOrFallback(playerData);
                })
                .orElse(playerId.toString());
    }

    public String playerName(UUID playerId) {
        if (playerId == null) {
            return "Unknown";
        }

        PlatformPlayer onlinePlayer = platform.getPlayer(playerId);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        PlayerData cachedData = playerService.getPlayerData(playerId);
        if (cachedData != null) {
            remember(cachedData);
            return playerNameOrFallback(cachedData);
        }

        PlayerData knownData = knownPlayersById.get(playerId);
        if (knownData != null) {
            playerService.putPlayerData(knownData);
            return playerNameOrFallback(knownData);
        }

        return databaseService.find(playerId, PlayerData.class)
                .map(playerData -> {
                    playerService.putPlayerData(playerData);
                    remember(playerData);
                    return playerNameOrFallback(playerData);
                })
                .orElse(playerId.toString());
    }

    public List<String> suggestFriendshipPlayerNames(Collection<FriendshipData> friendships, UUID viewerId, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        return friendships.stream()
                .map(friendship -> playerName(friendship.getOtherPlayerId(viewerId)))
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> suggestOnlinePlayers(UUID senderId, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<KnownPlayer> players = new ArrayList<>();
        for (PlayerData playerData : knownPlayersById.values()) {
            if (playerData.getLastPlayerName() != null && !playerData.getLastPlayerName().isBlank()) {
                players.add(new KnownPlayer(playerData.getPlayerId(), displayNameOrFallback(playerData), playerData, false));
            }
        }
        for (PlatformPlayer onlinePlayer : platform.getOnlinePlayers()) {
            if (senderId == null || !onlinePlayer.getUniqueId().equals(senderId)) {
                players.add(new KnownPlayer(onlinePlayer.getUniqueId(), displayName(onlinePlayer), ensurePlayerData(onlinePlayer), true));
            }
        }

        return players.stream()
                .filter(player -> senderId == null || !player.playerId().equals(senderId))
                .collect(java.util.stream.Collectors.toMap(
                        KnownPlayer::playerId,
                        player -> player,
                        (first, second) -> second
                ))
                .values()
                .stream()
                .map(KnownPlayer::playerData)
                .filter(playerData -> playerData.getLastPlayerName() != null && !playerData.getLastPlayerName().isBlank())
                .map(PlayerData::getLastPlayerName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
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
        playerData.setLastPlayerName(player.getName());
        playerService.putPlayerData(playerData);
        remember(playerData);
        return playerData;
    }

    private PlayerData knownPlayerByName(String name) {
        UUID playerId = playerIdsByLowerName.get(name.toLowerCase(Locale.ROOT));
        return playerId == null ? null : knownPlayersById.get(playerId);
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

        String playerName = playerData.getLastPlayerName();
        if (playerName != null && !playerName.isBlank()) {
            return playerName;
        }

        return playerData.getPlayerId().toString();
    }

    private String playerNameOrFallback(PlayerData playerData) {
        String playerName = playerData.getLastPlayerName();
        if (playerName != null && !playerName.isBlank()) {
            return playerName;
        }

        return playerData.getPlayerId().toString();
    }

    public record KnownPlayer(UUID playerId, String displayName, PlayerData playerData, boolean online) {
    }
}
