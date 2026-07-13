package com.trickshotmlg.friendnet.adapter_spigot.GUIs;

import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendNameSearchGUI extends AbstractGUI {
    private static final Map<UUID, FriendNameSearchGUI> PENDING_CHAT_INPUTS = new ConcurrentHashMap<>();
    private final FriendFilterState filterState;

    public FriendNameSearchGUI(JavaPlugin plugin, Player player, FriendFilterState filterState) {
        super(plugin, player, 0, "titles.friendNameSearchGUI");
        this.filterState = filterState;
    }

    @Override
    public void open() {
        startChatInput();
    }

    @Override
    protected void buildInventory() {
        // TODO: Revisit an anvil-based text input later. Bukkit.createInventory(..., ANVIL)
        // did not expose the renamed text through AnvilView#getRenameText on Paper 1.21.
    }

    public static boolean handleChatInput(AsyncPlayerChatEvent event, JavaPlugin plugin) {
        FriendNameSearchGUI gui = PENDING_CHAT_INPUTS.remove(event.getPlayer().getUniqueId());
        if (gui == null) {
            return false;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!gui.isCancelMessage(message)) {
                gui.applySearchQuery(message);
            }
            gui.goBack();
        });
        return true;
    }

    private boolean applySearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        query = ChatColor.stripColor(query).trim();
        if (query.isBlank()) {
            return false;
        }

        filterState.setNameSearchQuery(query);
        return true;
    }

    private void startChatInput() {
        PENDING_CHAT_INPUTS.put(player.getUniqueId(), this);
        player.closeInventory();
        player.sendMessage(locale("friendNameSearchGUI.prompt"));
    }

    private boolean isCancelMessage(String message) {
        String cancelKeyword = ChatColor.stripColor(locale("friendNameSearchGUI.cancelKeyword")).trim();
        return message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase(cancelKeyword);
    }

    private String locale(String key) {
        return FriendNetPlugin.LocaleManager.getMessage(player.getUniqueId(), "gui", key);
    }
}
