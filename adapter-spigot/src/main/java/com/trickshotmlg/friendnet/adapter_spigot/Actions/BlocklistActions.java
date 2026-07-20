package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.application.BlocklistApplicationService;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlocklistActions {

    private final FriendNetPlugin plugin;
    private final BlocklistApplicationService blocklistService;

    public BlocklistActions(FriendNetPlugin plugin) {
        this.plugin = plugin;
        this.blocklistService = plugin.getApplicationServices().blocklistService();
    }

    public List<BlocklistData> getBlockedPlayers(UUID blockerId) {
        return blocklistService.getBlockedPlayers(blockerId);
    }

    public boolean isBlocked(UUID blockerId, UUID blockedId) {
        return blocklistService.isBlocked(blockerId, blockedId);
    }

    public boolean hasEitherBlocked(UUID firstPlayerId, UUID secondPlayerId) {
        return blocklistService.hasEitherBlocked(firstPlayerId, secondPlayerId);
    }

    public boolean block(Player blocker, UUID blockedId) {
        return switch (blocklistService.block(blocker.getUniqueId(), blockedId)) {
            case BLOCKED -> {
                MessageManager.send(blocker, "blocklist.block.success", Map.of("target", getDisplayName(blockedId)));
                yield true;
            }
            case ALREADY_BLOCKED -> {
                MessageManager.send(blocker, "blocklist.block.already", Map.of("target", getDisplayName(blockedId)));
                yield false;
            }
            case SELF -> {
                MessageManager.send(blocker, "blocklist.block.self");
                yield false;
            }
        };
    }

    public boolean unblock(Player blocker, UUID blockedId) {
        blocklistService.unblock(blocker.getUniqueId(), blockedId);
        MessageManager.send(blocker, "blocklist.unblock.success", Map.of("target", getDisplayName(blockedId)));
        return true;
    }

    public int clear(Player blocker) {
        int count = blocklistService.clear(blocker.getUniqueId());
        MessageManager.send(blocker, "blocklist.clear.success", Map.of("count", count));
        return count;
    }

    private String getDisplayName(UUID playerId) {
        return KnownPlayerResolver.displayName(plugin, playerId);
    }
}
