package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.interfaces.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerServiceImpl implements PlayerService {

    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    /**
     * @param playerId
     */
    @Override
    public void initPlayer(UUID playerId) {
        if (players.containsKey(playerId)) {
            setLastSeen(playerId);
        }
        else {
            players.put(playerId, new PlayerData(playerId));
        }
        System.out.println(players.get(playerId));
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
}
