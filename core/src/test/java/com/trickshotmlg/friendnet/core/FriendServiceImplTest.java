package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FriendServiceImplTest extends TestCase {

    private UUID player;
    private UUID target;
    private FakeDatabaseService databaseService;
    private FriendServiceImpl friendService;

    @Override
    protected void setUp() {
        player = UUID.randomUUID();
        target = UUID.randomUUID();
        databaseService = new FakeDatabaseService();
        friendService = new FriendServiceImpl(databaseService, new PlayerServiceImpl());
    }

    public void testSendAndAcceptFriendRequest() {
        assertTrue(friendService.sendFriendRequest(player, target));
        assertTrue(friendService.requestPending(target, player));
        assertEquals(1, databaseService.savedFriendships.size());

        assertTrue(friendService.acceptFriendRequest(target, player));
        assertTrue(friendService.areFriends(player, target));
        assertEquals(1, friendService.getFriendships(player).size());
        assertEquals(2, databaseService.saveCalls);
    }

    public void testDenyFriendRequestRemovesIt() {
        assertTrue(friendService.sendFriendRequest(player, target));

        assertTrue(friendService.denyFriendRequest(target, player));
        assertFalse(friendService.requestPending(target, player));
        assertEquals(0, friendService.getPendingRequests(target).size());
        assertEquals(1, databaseService.deletedFriendships.size());
    }

    public void testCancelRequestOnlyWorksForRequester() {
        assertTrue(friendService.sendFriendRequest(player, target));

        assertFalse(friendService.cancelRequest(target, player));
        assertTrue(friendService.requestPending(target, player));

        assertTrue(friendService.cancelRequest(player, target));
        assertFalse(friendService.requestPending(target, player));
        assertEquals(1, databaseService.deletedFriendships.size());
    }

    public void testRemoveAcceptedFriendship() {
        assertTrue(friendService.sendFriendRequest(player, target));
        assertTrue(friendService.acceptFriendRequest(target, player));

        assertTrue(friendService.removeFriend(player, target));
        assertFalse(friendService.areFriends(player, target));
        assertEquals(1, databaseService.deletedFriendships.size());
    }

    private static class FakeDatabaseService implements DatabaseService {
        private final Set<FriendshipData> savedFriendships = new HashSet<>();
        private final Set<FriendshipData> deletedFriendships = new HashSet<>();
        private int saveCalls;

        @Override
        public Database getDatabase() {
            return null;
        }

        @Override
        public <T> Optional<T> find(UUID playerId, Class<T> clazz) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<Set<T>> findAll(UUID playerId, Class<T> clazz) {
            return Optional.empty();
        }

        @Override
        public Optional<PlayerData> findPlayerByLastDisplayName(String lastDisplayName) {
            return Optional.empty();
        }

        @Override
        public void save(FriendshipData entity) {
            savedFriendships.remove(entity);
            savedFriendships.add(entity);
            saveCalls++;
        }

        @Override
        public void save(PlayerData entity) {
        }

        @Override
        public void save(BlocklistData entity) {
        }

        @Override
        public void delete(FriendshipData entity) {
            savedFriendships.remove(entity);
            deletedFriendships.add(entity);
        }

        @Override
        public void delete(BlocklistData entity) {
        }

        @Override
        public void delete(PlayerData entity) {
        }

        @Override
        public void init() {
        }

        @Override
        public void postInit() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void destroy() {
        }

        @Override
        public ServiceState getState() {
            return ServiceState.STARTED;
        }

        @Override
        public boolean isRunning() {
            return true;
        }
    }
}
