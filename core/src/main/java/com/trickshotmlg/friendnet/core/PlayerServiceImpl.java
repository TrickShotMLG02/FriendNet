package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerServiceImpl implements PlayerService {

    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final Set<UUID> onlinePlayers = ConcurrentHashMap.newKeySet();

    /**
     * @param playerId
     */
    @Override
    public PlayerData initPlayer(UUID playerId) {
        if (!players.containsKey(playerId)) {
            players.put(playerId, new PlayerData(playerId));
        }

        return players.get(playerId);
    }

    /**
     * @param playerId
     */
    @Override
    public void setLastSeen(UUID playerId) {
        players.get(playerId).setLastSeen();
    }

    /**
     * @param playerId
     */
    @Override
    public Timestamp getLastSeen(UUID playerId) {
        return players.get(playerId).getLastSeen();
    }

    /**
     * @param playerId
     * @return
     */
    @Override
    public boolean isOnline(UUID playerId) {
        return onlinePlayers.contains(playerId)
                && players.containsKey(playerId)
                && getPlayerData(playerId).isShowOnlineStatus();
    }

    @Override
    public boolean setOnline(UUID playerId, boolean online) {
        if (online) {
            onlinePlayers.add(playerId);
        } else {
            onlinePlayers.remove(playerId);
        }
        return true;
    }

    /**
     * @param playerId
     * @return
     */
    @Override
    public PlayerData getPlayerData(UUID playerId) {
        return players.get(playerId);
    }

    /**
     * @param playerData
     * @return
     */
    @Override
    public boolean putPlayerData(PlayerData playerData) {
        players.put(playerData.getPlayerId(), playerData);
        return true;
    }

    /**
     * @param playerId
     * @return
     */
    @Override
    public boolean removePlayerData(UUID playerId) {
        players.remove(playerId);
        onlinePlayers.remove(playerId);
        return true;
    }

}
