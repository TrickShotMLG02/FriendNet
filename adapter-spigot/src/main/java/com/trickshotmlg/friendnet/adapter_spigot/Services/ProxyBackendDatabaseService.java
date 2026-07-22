package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FavouriteData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary backend-mode database adapter.
 * Persistent state is owned by the proxy; backend reads/writes are intentionally inert
 * until the backend-to-proxy request bridge is implemented.
 */
public class ProxyBackendDatabaseService implements DatabaseService {

    private ServiceState state = ServiceState.NEW;
    private final Set<String> reportedOperations = ConcurrentHashMap.newKeySet();

    @Override
    public Database getDatabase() {
        reportUnexpectedUse("getDatabase");
        throw new UnsupportedOperationException("Proxy backend mode does not expose a local database.");
    }

    @Override
    public <T> Optional<T> find(UUID playerId, Class<T> clazz) {
        reportUnexpectedUse("find(" + typeName(clazz) + ")");
        return Optional.empty();
    }

    @Override
    public <T> Optional<Set<T>> findAll(UUID playerId, Class<T> clazz) {
        reportUnexpectedUse("findAll(" + typeName(clazz) + ")");
        return Optional.of(Set.of());
    }

    @Override
    public Optional<PlayerData> findPlayerByLastDisplayName(String lastDisplayName) {
        reportUnexpectedUse("findPlayerByLastDisplayName");
        return Optional.empty();
    }

    @Override
    public Optional<PlayerData> findPlayerByLastPlayerName(String lastPlayerName) {
        reportUnexpectedUse("findPlayerByLastPlayerName");
        return Optional.empty();
    }

    @Override
    public void save(FriendshipData entity) {
        reportUnexpectedUse("save(FriendshipData)");
    }

    @Override
    public void save(BlocklistData entity) {
        reportUnexpectedUse("save(BlocklistData)");
    }

    @Override
    public void save(FavouriteData entity) {
        reportUnexpectedUse("save(FavouriteData)");
    }

    @Override
    public void save(PlayerData entity) {
        reportUnexpectedUse("save(PlayerData)");
    }

    @Override
    public void delete(FriendshipData entity) {
        reportUnexpectedUse("delete(FriendshipData)");
    }

    @Override
    public void delete(BlocklistData entity) {
        reportUnexpectedUse("delete(BlocklistData)");
    }

    @Override
    public void delete(FavouriteData entity) {
        reportUnexpectedUse("delete(FavouriteData)");
    }

    @Override
    public void delete(PlayerData entity) {
        reportUnexpectedUse("delete(PlayerData)");
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

    private void reportUnexpectedUse(String operation) {
        if (!reportedOperations.add(operation)) {
            return;
        }

        Logger.error(
                "Unexpected local database access in Spigot proxy-backend mode: " + operation,
                new IllegalStateException("Spigot proxy-backend mode must use the proxy as database authority.")
        );
    }

    private String typeName(Class<?> clazz) {
        return clazz == null ? "unknown" : clazz.getSimpleName();
    }
}
