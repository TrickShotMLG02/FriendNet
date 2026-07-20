package com.trickshotmlg.friendnet.core.application;

import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BlocklistApplicationService {

    private final DatabaseService databaseService;
    private final FriendService friendService;
    private final KnownPlayerLookup knownPlayerLookup;

    public BlocklistApplicationService(
            DatabaseService databaseService,
            FriendService friendService,
            KnownPlayerLookup knownPlayerLookup
    ) {
        this.databaseService = databaseService;
        this.friendService = friendService;
        this.knownPlayerLookup = knownPlayerLookup;
    }

    public List<BlocklistData> getBlockedPlayers(UUID blockerId) {
        return databaseService.findAll(blockerId, BlocklistData.class)
                .orElse(Set.of())
                .stream()
                .sorted(Comparator.comparing(blocked -> knownPlayerLookup.displayName(blocked.getBlockedId()).toLowerCase()))
                .toList();
    }

    public boolean isBlocked(UUID blockerId, UUID blockedId) {
        return getBlockedPlayers(blockerId).stream()
                .anyMatch(blocked -> blocked.getBlockedId().equals(blockedId));
    }

    public boolean hasEitherBlocked(UUID firstPlayerId, UUID secondPlayerId) {
        return isBlocked(firstPlayerId, secondPlayerId) || isBlocked(secondPlayerId, firstPlayerId);
    }

    public BlockResult block(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            return BlockResult.SELF;
        }

        if (isBlocked(blockerId, blockedId)) {
            return BlockResult.ALREADY_BLOCKED;
        }

        BlocklistData blocklistData = new BlocklistData(blockerId, blockedId);
        databaseService.save(blocklistData);
        friendService.getFriendshipData(blockerId, blockedId).ifPresent(friendship -> {
            friendService.removeFriendshipData(friendship);
            databaseService.delete(friendship);
        });

        return BlockResult.BLOCKED;
    }

    public boolean unblock(UUID blockerId, UUID blockedId) {
        databaseService.delete(new BlocklistData(blockerId, blockedId));
        return true;
    }

    public int clear(UUID blockerId) {
        List<BlocklistData> blockedPlayers = getBlockedPlayers(blockerId);
        blockedPlayers.forEach(databaseService::delete);
        return blockedPlayers.size();
    }

    public enum BlockResult {
        BLOCKED,
        ALREADY_BLOCKED,
        SELF
    }
}
