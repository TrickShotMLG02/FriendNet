package com.trickshotmlg.friendnet.adapter_spigot.Actions;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.KnownPlayerResolver;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotCommandResultRenderer;
import com.trickshotmlg.friendnet.core.application.BlocklistApplicationService;
import com.trickshotmlg.friendnet.core.application.command.FriendCommandUseCases;
import com.trickshotmlg.friendnet.core_api.models.BlocklistData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class BlocklistActions {

    private final FriendNetPlugin plugin;
    private final BlocklistApplicationService blocklistService;
    private final FriendCommandUseCases commandUseCases;

    public BlocklistActions(FriendNetPlugin plugin) {
        this.plugin = plugin;
        this.blocklistService = plugin.getApplicationServices().blocklistService();
        this.commandUseCases = plugin.getApplicationServices().friendCommandUseCases();
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
        var result = commandUseCases.blockPlayer(blocker.getUniqueId(), blockedId, getDisplayName(blockedId));
        SpigotCommandResultRenderer.render(blocker, result);
        return result.success();
    }

    public boolean unblock(Player blocker, UUID blockedId) {
        var result = commandUseCases.unblockPlayer(blocker.getUniqueId(), blockedId, getDisplayName(blockedId));
        SpigotCommandResultRenderer.render(blocker, result);
        return result.success();
    }

    public int clear(Player blocker) {
        int count = getBlockedPlayers(blocker.getUniqueId()).size();
        var result = commandUseCases.clearBlocklist(blocker.getUniqueId());
        SpigotCommandResultRenderer.render(blocker, result);
        return count;
    }

    private String getDisplayName(UUID playerId) {
        return KnownPlayerResolver.displayName(plugin, playerId);
    }
}
