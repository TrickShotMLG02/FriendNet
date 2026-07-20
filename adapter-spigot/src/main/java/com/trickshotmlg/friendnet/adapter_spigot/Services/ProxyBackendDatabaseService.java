package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Temporary backend-mode database adapter.
 * Persistent state is owned by the proxy; backend reads/writes are intentionally inert
 * until the backend-to-proxy request bridge is implemented.
 */
public class ProxyBackendDatabaseService implements DatabaseService {

    private ServiceState state = ServiceState.NEW;

    @Override
    public Database getDatabase() {
        throw new UnsupportedOperationException("Proxy backend mode does not expose a local database.");
    }

    @Override
    public <T> Optional<T> find(UUID playerId, Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<Set<T>> findAll(UUID playerId, Class<T> clazz) {
        return Optional.of(Set.of());
    }

    @Override
    public Optional<PlayerData> findPlayerByLastDisplayName(String lastDisplayName) {
        return Optional.empty();
    }

    @Override
    public void save(FriendshipData entity) {
    }

    @Override
    public void save(BlocklistData entity) {
    }

    @Override
    public void save(PlayerData entity) {
    }

    @Override
    public void delete(FriendshipData entity) {
    }

    @Override
    public void delete(BlocklistData entity) {
    }

    @Override
    public void delete(PlayerData entity) {
    }

    @Override
    public void init() {
        state = ServiceState.INITIALIZED;
    }

    @Override
    public void postInit() {
    }

    @Override
    public void start() {
        state = ServiceState.STARTED;
    }

    @Override
    public void stop() {
        state = ServiceState.STOPPED;
    }

    @Override
    public void destroy() {
        state = ServiceState.DESTROYED;
    }

    @Override
    public ServiceState getState() {
        return state;
    }

    @Override
    public boolean isRunning() {
        return state == ServiceState.STARTED;
    }
}
