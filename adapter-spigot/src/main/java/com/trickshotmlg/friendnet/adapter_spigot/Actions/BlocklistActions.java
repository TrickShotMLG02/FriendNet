package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotUtils;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlocklistActions {

    private final FriendNetPlugin plugin;
    private final DatabaseService databaseService;
    private final FriendService friendService;

    public BlocklistActions(FriendNetPlugin plugin) {
        this.plugin = plugin;
        this.databaseService = plugin.getDatabaseService();
        this.friendService = plugin.getFriendService();
    }

    public List<BlocklistData> getBlockedPlayers(UUID blockerId) {
        return databaseService.findAll(blockerId, BlocklistData.class)
                .orElse(Set.of())
                .stream()
                .sorted(Comparator.comparing(blocked -> getDisplayName(blocked.getBlockedId()).toLowerCase()))
                .toList();
    }

    public boolean isBlocked(UUID blockerId, UUID blockedId) {
        return getBlockedPlayers(blockerId).stream()
                .anyMatch(blocked -> blocked.getBlockedId().equals(blockedId));
    }

    public boolean hasEitherBlocked(UUID firstPlayerId, UUID secondPlayerId) {
        return isBlocked(firstPlayerId, secondPlayerId) || isBlocked(secondPlayerId, firstPlayerId);
    }

    public boolean block(Player blocker, UUID blockedId) {
        if (blocker.getUniqueId().equals(blockedId)) {
            MessageManager.send(blocker, "blocklist.block.self");
            return false;
        }

        if (isBlocked(blocker.getUniqueId(), blockedId)) {
            MessageManager.send(blocker, "blocklist.block.already", Map.of("target", getDisplayName(blockedId)));
            return false;
        }

        BlocklistData blocklistData = new BlocklistData(blocker.getUniqueId(), blockedId);
        databaseService.save(blocklistData);
        friendService.getFriendshipData(blocker.getUniqueId(), blockedId).ifPresent(friendship -> {
            friendService.removeFriendshipData(friendship);
            databaseService.delete(friendship);
        });

        MessageManager.send(blocker, "blocklist.block.success", Map.of("target", getDisplayName(blockedId)));
        return true;
    }

    public boolean unblock(Player blocker, UUID blockedId) {
        BlocklistData blocklistData = new BlocklistData(blocker.getUniqueId(), blockedId);
        databaseService.delete(blocklistData);
        MessageManager.send(blocker, "blocklist.unblock.success", Map.of("target", getDisplayName(blockedId)));
        return true;
    }

    public int clear(Player blocker) {
        List<BlocklistData> blockedPlayers = getBlockedPlayers(blocker.getUniqueId());
        blockedPlayers.forEach(databaseService::delete);
        MessageManager.send(blocker, "blocklist.clear.success", Map.of("count", blockedPlayers.size()));
        return blockedPlayers.size();
    }

    private String getDisplayName(UUID playerId) {
        String displayName = SpigotUtils.getPlayerDisplayName(plugin, playerId);
        return displayName.isBlank() ? playerId.toString() : displayName;
    }
}
