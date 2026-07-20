package com.trickshotmlg.friendnet.core.application;

import com.trickshotmlg.friendnet.core.FriendServiceImpl;
import com.trickshotmlg.friendnet.core.PlayerServiceImpl;
import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core.application.command.CommandEventType;
import com.trickshotmlg.friendnet.core.application.command.CommandMessageRecipient;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ApplicationServiceTest extends TestCase {

    public void testFriendRequestServiceAcceptsIncomingRequestInsteadOfDuplicating() {
        FriendServiceImpl friendService = new FriendServiceImpl(new FakeDatabaseService(), new PlayerServiceImpl());
        FriendRequestApplicationService requestService = new FriendRequestApplicationService(friendService, null);
        UUID player = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        PlayerData playerData = new PlayerData(player);
        PlayerData targetData = new PlayerData(target);

        assertEquals(
                FriendRequestApplicationService.SendRequestResult.SENT,
                requestService.sendFriendRequest(player, new KnownPlayerLookup.KnownPlayer(target, "Target", targetData, false))
        );
        assertEquals(
                FriendRequestApplicationService.SendRequestResult.ACCEPTED_INCOMING,
                requestService.sendFriendRequest(target, new KnownPlayerLookup.KnownPlayer(player, "Player", playerData, false))
        );
        assertTrue(friendService.areFriends(player, target));
    }

    public void testFriendRequestServiceRespectsAutoAccept() {
        FriendServiceImpl friendService = new FriendServiceImpl(new FakeDatabaseService(), new PlayerServiceImpl());
        FriendRequestApplicationService requestService = new FriendRequestApplicationService(friendService, null);
        UUID player = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        PlayerData targetData = new PlayerData(target);
        targetData.setAutoAcceptFriends(true);

        assertEquals(
                FriendRequestApplicationService.SendRequestResult.AUTO_ACCEPTED,
                requestService.sendFriendRequest(player, new KnownPlayerLookup.KnownPlayer(target, "Target", targetData, false))
        );
        assertTrue(friendService.areFriends(player, target));
    }

    public void testPlayerSettingsServiceMarksDirtyAndNotifiesVisibilityChanges() {
        PlayerServiceImpl playerService = new PlayerServiceImpl();
        UUID player = UUID.randomUUID();
        playerService.initPlayer(player);
        playerService.setOnline(player, true);
        Set<UUID> dirtyPlayers = new HashSet<>();
        RecordingStatusNotifier notifier = new RecordingStatusNotifier();
        PlayerSettingsApplicationService settingsService = new PlayerSettingsApplicationService(
                playerService,
                dirtyPlayers::add,
                notifier
        );

        assertTrue(settingsService.setShowOnlineStatus(player, false));
        assertTrue(dirtyPlayers.contains(player));
        assertEquals(1, notifier.offlineNotifications);
        assertEquals(0, notifier.onlineNotifications);
        assertFalse(playerService.isOnline(player));

        assertTrue(settingsService.setShowOnlineStatus(player, true));
        assertEquals(1, notifier.offlineNotifications);
        assertEquals(1, notifier.onlineNotifications);
        assertTrue(playerService.isOnline(player));
    }

    public void testFriendCommandUseCaseReturnsMessagesAndEventsForFriendAdd() {
        PlayerServiceImpl playerService = new PlayerServiceImpl();
        FriendServiceImpl friendService = new FriendServiceImpl(new FakeDatabaseService(), playerService);
        FriendRequestApplicationService requestService = new FriendRequestApplicationService(friendService, null);
        FriendCommandUseCases useCases = new FriendCommandUseCases(friendService, requestService, null, null);
        UUID sender = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        PlayerData targetData = new PlayerData(target);

        var result = useCases.sendFriendRequest(
                sender,
                "Sender",
                new KnownPlayerLookup.KnownPlayer(target, "Target", targetData, true)
        );

        assertTrue(result.success());
        assertEquals(1, result.messages().size());
        assertEquals(CommandMessageRecipient.SENDER, result.messages().get(0).recipient());
        assertEquals("friendRequest.send.sender.success", result.messages().get(0).key());
        assertEquals(1, result.events().size());
        assertEquals(CommandEventType.FRIEND_REQUEST_SENT, result.events().get(0).type());
        assertEquals(sender, result.events().get(0).actorId());
        assertEquals(target, result.events().get(0).targetId());
    }

    private static class RecordingStatusNotifier implements FriendStatusVisibilityNotifier {
        private int onlineNotifications;
        private int offlineNotifications;

        @Override
        public void notifyOnline(UUID playerId) {
            onlineNotifications++;
        }

        @Override
        public void notifyOffline(UUID playerId) {
            offlineNotifications++;
        }
    }

    private static class FakeDatabaseService implements DatabaseService {
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
