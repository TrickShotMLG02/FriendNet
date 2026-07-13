package com.trickshotmlg.friendnet.core_api.interfaces.services;

import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DatabaseService extends BaseService {

    Database getDatabase();

    public <T> Optional<T> find(UUID playerId, Class<T> clazz);
    public <T> Optional<Set<T>> findAll(UUID playerId, Class<T> clazz);
    public Optional<PlayerData> findPlayerByLastDisplayName(String lastDisplayName);

    public void save(FriendshipData entity);
    public void save(BlocklistData entity);
    public void save(PlayerData entity);

    public void delete(FriendshipData entity);
    public void delete(BlocklistData entity);
    public void delete(PlayerData entity);
}
