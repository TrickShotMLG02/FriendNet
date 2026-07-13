package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.Actions.BlocklistActions;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockPlayerInputGUI extends AbstractGUI {
    private static final Map<UUID, BlockPlayerInputGUI> PENDING_CHAT_INPUTS = new ConcurrentHashMap<>();

    public BlockPlayerInputGUI(JavaPlugin plugin, Player player) {
        super(plugin, player, 0, "titles.blocklistGUI");
    }

    @Override
    public void open() {
        startChatInput();
    }

    @Override
    protected void buildInventory() {
    }

    public static boolean handleChatInput(AsyncPlayerChatEvent event, JavaPlugin plugin) {
        BlockPlayerInputGUI gui = PENDING_CHAT_INPUTS.remove(event.getPlayer().getUniqueId());
        if (gui == null) {
            return false;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!gui.isCancelMessage(message)) {
                gui.blockPlayer(message);
            }
            gui.goBack();
        });
        return true;
    }

    private void blockPlayer(String rawName) {
        String targetName = ChatColor.stripColor(rawName).trim();
        if (targetName.isBlank()) {
            return;
        }

        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        UUID targetId;
        String targetDisplayName = targetName;
        if (onlineTarget != null) {
            targetId = onlineTarget.getUniqueId();
            targetDisplayName = onlineTarget.getName();
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            targetId = offlineTarget.getUniqueId();
            if (offlineTarget.getName() != null && !offlineTarget.getName().isBlank()) {
                targetDisplayName = offlineTarget.getName();
            }
        }

        ensurePlayerData(targetId, targetDisplayName);
        new BlocklistActions((FriendNetPlugin) plugin).block(player, targetId);
    }

    private void ensurePlayerData(UUID targetId, String targetDisplayName) {
        FriendNetPlugin friendNetPlugin = (FriendNetPlugin) plugin;
        if (friendNetPlugin.getPlayerService().getPlayerData(targetId) != null) {
            return;
        }

        PlayerData playerData = friendNetPlugin.getDatabaseService()
                .find(targetId, PlayerData.class)
                .orElseGet(() -> new PlayerData(targetId));
        playerData.setLastDisplayName(targetDisplayName);
        friendNetPlugin.getPlayerService().putPlayerData(playerData);
        friendNetPlugin.getDatabaseService().save(playerData);
    }

    private void startChatInput() {
        PENDING_CHAT_INPUTS.put(player.getUniqueId(), this);
        player.closeInventory();
        player.sendMessage(locale("blocklistGUI.chatInput.prompt"));
    }

    private boolean isCancelMessage(String message) {
        String cancelKeyword = ChatColor.stripColor(locale("blocklistGUI.chatInput.cancelKeyword")).trim();
        return message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase(cancelKeyword);
    }

    private String locale(String key) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key);
    }
}
