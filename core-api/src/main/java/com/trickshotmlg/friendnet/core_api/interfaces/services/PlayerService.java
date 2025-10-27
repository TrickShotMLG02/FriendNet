package com.trickshotmlg.friendnet.core_api.interfaces.services;

import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.sql.Timestamp;
import java.util.UUID;

public interface PlayerService {

    public PlayerData initPlayer(UUID playerId);

    /**
     * Sets the last seen timestamp to the current time for given player
     * @param playerId The player to set last seen
     */
    public void setLastSeen(UUID playerId);

    public Timestamp getLastSeen(UUID playerId);

    public PlayerData getPlayerData(UUID playerId);

    public boolean putPlayerData(PlayerData playerData);

    public boolean removePlayerData(UUID playerId);
}
